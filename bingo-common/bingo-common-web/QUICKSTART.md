# RequestId 全链路追踪 - 快速开始

## 🚀 5 分钟快速上手

### 1. 核心功能（开箱即用）

RequestId 全链路追踪已经集成到框架中，**无需任何配置**即可使用：

✅ **自动生效**:
- 所有 HTTP 请求自动生成唯一 RequestId
- 所有日志自动包含 RequestId
- 响应头自动返回 RequestId

### 2. 查看效果

#### 启动应用后，发起一个请求：

```bash
curl http://localhost:8080/api/test
```

#### 查看控制台日志：

```
2026-03-14 10:30:15 [virtual-threads-1] INFO  c.b.a.TestController [a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6] 测试接口被调用
2026-03-14 10:30:15 [virtual-threads-1] INFO  c.b.c.w.f.RequestLogFilter [a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6] 
========== 请求开始 ==========
请求方法：GET
请求 URL：http://localhost:8080/api/test
追踪 ID：a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
...
```

**说明**: 方括号中的 `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6` 就是 RequestId

### 3. 根据 RequestId 排查问题

#### 在日志文件中搜索：

```powershell
# Windows PowerShell
Select-String -Path "logs/sys-info.log" -Pattern "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
```

```bash
# Linux/Mac
grep "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6" logs/sys-info.log
```

**效果**: 可以看到该请求的所有相关日志，包括：
- 请求信息
- 业务逻辑处理
- SQL 执行
- 异常信息
- 响应信息

## 📦 已创建的文件

### 核心组件

| 文件 | 路径 | 说明 |
|------|------|------|
| `RequestIdFilter.java` | `bingo-common-web/src/main/java/com/bingo/common/web/filter/` | RequestId 过滤器（最高优先级） |
| `RequestIdUtils.java` | `bingo-common-web/src/main/java/com/bingo/common/web/utils/` | RequestId 工具类 |
| `AsyncRequestIdUtils.java` | `bingo-common-web/src/main/java/com/bingo/common/web/utils/` | 异步任务包装器 |
| `RequestLogFilter.java` | `bingo-common-web/src/main/java/com/bingo/common/web/filter/` | 请求日志过滤器（已集成 RequestId） |

### 配置文件

| 文件 | 修改内容 |
|------|----------|
| `Constants.java` | 添加 RequestId 相关常量 |
| `logback-spring.xml` | 日志格式增加 RequestId 显示 |

### 文档和示例

| 文件 | 路径 | 说明 |
|------|------|------|
| `README-RequestId 全链路追踪.md` | `bingo-common-web/` | 完整使用文档 |
| `RequestIdExample.java` | `bingo-common-web/src/test/java/` | 使用示例代码 |

## 🔧 常用场景

### 场景 1: 普通业务代码（无需额外操作）

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @PostMapping
    public R<User> create(@RequestBody User user) {
        log.info("创建用户：{}", user.getName());
        // 日志自动包含 RequestId
        return R.ok(user);
    }
}
```

### 场景 2: 异步任务（需要包装）

```java
@Service
public class UserService {
    
    @Autowired
    private ThreadPoolTaskExecutor executor;
    
    public void createUserAsync(User user) {
        // ✅ 正确：使用 AsyncRequestIdUtils.wrapRunnable
        executor.execute(AsyncRequestIdUtils.wrapRunnable(() -> {
            log.info("异步创建用户：{}", user.getName());
            // 这里可以获取到主线程的 RequestId
        }));
    }
}
```

### 场景 3: 自定义 RequestId（可选）

```bash
# 前端传递自定义 RequestId
curl -H "X-Request-ID: my-custom-id-123" http://localhost:8080/api/users
```

## 🎯 核心价值

✅ **问题定位快**: 通过一个 RequestId 可以看到请求的完整链路
✅ **日志关联强**: 所有日志通过 RequestId 串联
✅ **异步可追踪**: 异步任务的日志也能关联到主请求
✅ **性能影响小**: MDC 操作纳秒级，几乎无性能损耗

## 📖 更多信息

详细文档请查看：[README-RequestId 全链路追踪.md](./README-RequestId 全链路追踪.md)

使用示例请参考：[RequestIdExample.java](./src/test/java/com/bingo/common/web/utils/RequestIdExample.java)

## ⚠️ 重要提示

### 异步任务必须包装！

```java
// ❌ 错误：RequestId 不会传递
executor.execute(() -> {
    log.info("这里获取不到 RequestId");
});

// ✅ 正确：RequestId 会传递
executor.execute(AsyncRequestIdUtils.wrapRunnable(() -> {
    log.info("这里可以获取到 RequestId");
}));
```

---

**🎉 恭喜！您已掌握 RequestId 全链路追踪的基本用法！**
