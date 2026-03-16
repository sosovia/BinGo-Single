# RequestId 全链路追踪使用说明

## 概述

本项目通过 `RequestId` 实现全链路追踪，确保每个请求都有唯一标识，并贯穿整个请求生命周期（含日志打印、下游 HTTP 调用、异步任务等），实现日志可关联、问题可定位。

## 核心组件

### 1. RequestIdFilter（请求追踪过滤器）

**位置**: `com.bingo.common.web.filter.RequestIdFilter`

**功能**:
- 在请求入口生成或提取 RequestId
- 将 RequestId 设置到 MDC（Mapped Diagnostic Context）
- 在响应头中返回 RequestId，便于客户端追踪

**工作流程**:
1. 从请求头 `X-Request-ID` 尝试获取 RequestId（如果前端传递）
2. 如果没有则生成新的 RequestId（UUID，无横杠格式）
3. 设置 RequestId 到 MDC
4. 执行请求
5. 在响应头中添加 RequestId
6. 清除 MDC（防止线程池复用导致的污染）

**过滤器顺序**: `Ordered.HIGHEST_PRECEDENCE + 1`（最高优先级）

### 2. RequestIdUtils（工具类）

**位置**: `com.bingo.common.core.utils.RequestIdUtils`

**核心方法**:

```java
// 生成新的 RequestId
String requestId = RequestIdUtils.generate();

// 从 MDC 获取 RequestId
String requestId = RequestIdUtils.getFromMDC();

// 设置 RequestId 到 MDC
RequestIdUtils.setToMDC(requestId);

// 获取当前 RequestId（优先从 MDC 获取，否则生成新的）
String requestId = RequestIdUtils.getCurrent();

// 清除 MDC 中的 RequestId
RequestIdUtils.clearMDC();
```

### 3. AsyncRequestIdUtils（异步任务包装器）

**位置**: `com.bingo.common.web.utils.AsyncRequestIdUtils`

**功能**: 包装异步任务，使 RequestId 能在异步线程中传递

**使用示例**:

```java
// Runnable 包装
CompletableFuture.runAsync(
    AsyncRequestIdUtils.wrapRunnable(() -> {
        // 这里可以获取到 RequestId
        log.info("异步任务执行中");
    })
);

// Callable 包装
CompletableFuture.supplyAsync(
    AsyncRequestIdUtils.wrapCallable(() -> {
        log.info("带返回值的异步任务执行中");
        return "result";
    })
);

// Supplier 包装
CompletableFuture.supplyAsync(
    AsyncRequestIdUtils.wrapSupplier(() -> {
        log.info("Supplier 异步任务执行中");
        return "data";
    })
);
```

## 日志配置

### logback-spring.xml 配置

日志格式已更新，自动包含 RequestId：

```xml
<!-- 控制台日志格式 -->
<property name="console.log.pattern"
          value="%cyan(%d{yyyy-MM-dd HH:mm:ss}) %green([%thread]) %highlight(%-5level) %boldMagenta(%logger{36}%n) [%X{requestId:-N/A}] %msg%n"/>

<!-- 文件日志格式 -->
<property name="log.pattern" 
          value="%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [%X{requestId:-N/A}] %msg%n"/>
```

**说明**: 
- `%X{requestId:-N/A}` 表示从 MDC 中获取 `requestId`，如果不存在则显示 `N/A`
- 所有日志都会自动包含 RequestId，无需手动添加

### RequestLogFilter（请求日志过滤器）

已更新为在日志中包含 RequestId：

**请求日志示例**:
```
========== 请求开始 ==========
请求方法：POST
请求 URL：http://localhost:8080/api/users
请求 URI：/api/users
追踪 ID：a1b2c3d4e5f6g7h8i9j0
...
========== 请求结束 ==========
```

**响应日志示例**:
```
========== 响应开始 ==========
状态码：200
内容类型：application/json
处理耗时：125ms
追踪 ID：a1b2c3d4e5f6g7h8i9j0
========== 响应结束 ==========
```

**异常日志示例**:
```
ERROR 请求处理异常：[a1b2c3d4e5f6g7h8i9j0] POST /api/users - 耗时 50ms
```

## 使用场景

### 1. Web 请求追踪

所有 HTTP 请求会自动生成 RequestId，并在日志中体现：

```bash
# 发起请求（可选：自定义 RequestId）
curl -H "X-Request-ID: custom-request-id-123" http://localhost:8080/api/users

# 或者让系统自动生成
curl http://localhost:8080/api/users
```

### 2. 异步任务追踪

在线程池或异步任务中使用：

```java
@Service
public class UserService {
    
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    
    public void createUser(User user) {
        // 主线程中的 RequestId 会自动传递
        taskExecutor.execute(AsyncRequestIdUtils.wrapRunnable(() -> {
            // 这里可以获取到相同的 RequestId
            log.info("异步创建用户：{}", user.getName());
            // ... 业务逻辑
        }));
    }
}
```

### 3. 下游 HTTP 调用追踪

调用第三方服务时，可以传递 RequestId：

```java
@Service
public class ExternalApiClient {
    
    public ResponseEntity<String> callExternalApi() {
        String requestId = RequestIdUtils.getCurrent();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Request-ID", requestId);  // 传递给下游服务
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange("http://api.example.com/data", 
                                     HttpMethod.GET, 
                                     request, 
                                     String.class);
    }
}
```

### 4. 定时任务追踪

定时任务可以生成新的 RequestId：

```java
@Component
public class ScheduledTasks {
    
    @Scheduled(cron = "0 0 1 * * ?")
    public void dailyTask() {
        // 生成新的 RequestId
        String requestId = RequestIdUtils.generate();
        RequestIdUtils.setToMDC(requestId);
        
        try {
            log.info("执行每日任务");
            // ... 业务逻辑
        } finally {
            RequestIdUtils.clearMDC();
        }
    }
}
```

## 日志排查示例

### 场景 1：根据 RequestId 追踪完整请求链路

```bash
# 在日志文件中搜索特定 RequestId
grep "a1b2c3d4e5f6g7h8i9j0" logs/sys-info.log

# PowerShell 示例
Select-String -Path "logs/sys-info.log" -Pattern "a1b2c3d4e5f6g7h8i9j0"
```

### 场景 2：查看某个请求的所有日志

```bash
# Linux/Mac
cat logs/sys-info.log | grep "a1b2c3d4e5f6g7h8i9j0"

# Windows PowerShell
Get-Content logs/sys-info.log | Select-String "a1b2c3d4e5f6g7h8i9j0"
```

### 场景 3：在 ELK/Splunk 等日志平台查询

```
requestId: "a1b2c3d4e5f6g7h8i9j0"
```

## 常量定义

**位置**: `com.bingo.common.core.constant.Constants`

```java
/**
 * RequestId 请求追踪标识头
 */
String REQUEST_ID_HEADER = "X-Request-ID";

/**
 * RequestId MDC Key
 */
String REQUEST_ID_MDC_KEY = "requestId";
```

## 注意事项

### 1. 线程池复用问题

RequestIdFilter 会在请求结束后清除 MDC，防止线程池复用导致 RequestId 污染。

### 2. 异步任务必须包装

未包装的异步任务无法获取到当前请求的 RequestId：

```java
// ❌ 错误示例 - RequestId 不会传递
taskExecutor.execute(() -> {
    log.info("这里获取不到 RequestId");
});

// ✅ 正确示例 - RequestId 会传递
taskExecutor.execute(AsyncRequestIdUtils.wrapRunnable(() -> {
    log.info("这里可以获取到 RequestId");
}));
```

### 3. 日志性能

- MDC 操作非常轻量，对性能影响极小（纳秒级）
- 异步日志队列已配置为 512，避免阻塞主线程

### 4. RequestId 格式

- 默认使用 UUID（无横杠），32 位十六进制字符串
- 示例：`a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`

## 最佳实践

1. **前端传递 RequestId**: 建议前端在发起请求时携带 RequestId，便于端到端追踪
2. **微服务传递**: 在微服务架构中，将 RequestId 传递给下游服务
3. **异常必打**: 所有异常日志都会自动包含 RequestId，无需手动添加
4. **异步必包**: 所有异步任务必须使用 `AsyncRequestIdUtils` 包装

## 技术栈

- **SLF4J MDC**: 用于线程上下文管理
- **Logback**: 日志框架，支持 MDC 变量
- **Spring Filter**: 请求拦截和处理
- **UUID**: RequestId 生成算法

## 相关文件

- `com.bingo.common.web.filter.RequestIdFilter` - RequestId 过滤器
- `com.bingo.common.core.utils.RequestIdUtils` - RequestId 工具类
- `com.bingo.common.web.utils.AsyncRequestIdUtils` - 异步任务包装器
- `com.bingo.common.web.filter.RequestLogFilter` - 请求日志过滤器（已集成 RequestId）
- `com.bingo.common.core.constant.Constants` - 常量定义
- `bingo-app/src/main/resources/logback-spring.xml` - 日志配置
