# SpringBoot Web 请求日志配置说明

## 功能概述

`RequestLogFilter` 是一个用于记录所有 HTTP 请求详细信息的过滤器，可以帮助开发人员进行调试、监控和问题排查。

## 主要功能

### 1. 请求信息记录
- 请求方法（GET/POST/PUT/DELETE 等）
- 请求 URL 和 URI
- 查询参数
- 客户端 IP 地址和主机
- 协议信息

### 2. 请求头记录
- 记录所有 HTTP 请求头
- 自动隐藏敏感信息（Authorization、Cookie、Set-Cookie）

### 3. 请求参数记录
- 记录所有请求参数
- 自动隐藏敏感参数（包含 password、token、secret、key 等关键字的参数）

### 4. 响应信息记录
- HTTP 状态码
- 响应内容类型
- 响应内容长度
- 请求处理耗时

### 5. 慢请求检测
- 可配置的慢请求阈值
- 超过阈值的请求会打印警告日志

## 配置方式

在 `application.yml` 或 `application.properties` 中添加以下配置：

### YAML 格式

```yaml
request:
  log:
    # 是否启用请求日志，默认启用
    enabled: true
    # 是否记录请求头，默认记录
    logHeaders: true
    # 是否记录请求参数，默认记录
    logParameters: true
    # 是否隐藏敏感信息，默认隐藏
    hideSensitiveData: true
    # 慢请求阈值（毫秒），默认 1000ms
    slowThreshold: 1000
```

### Properties 格式

```properties
request.log.enabled=true
request.log.logHeaders=true
request.log.logParameters=true
request.log.hideSensitiveData=true
request.log.slowThreshold=1000
```

## 日志输出示例

### 请求日志
```
========== 请求开始 ==========
请求方法：POST
请求 URL：http://localhost:8080/api/user/login
请求 URI：/api/user/login
上下文路径：
查询参数：无
远程地址：127.0.0.1
远程主机：127.0.0.1
协议：HTTP/1.1
方案：http
是否 HTTPS：否
请求头:
  Content-Type: application/json
  User-Agent: Mozilla/5.0
  Authorization: ***
请求参数:
  username: admin
  password: ***
========== 请求结束 ==========
```

### 响应日志
```
========== 响应开始 ==========
状态码：200
内容类型：application/json
内容长度：256
处理耗时：156ms
========== 响应结束 ==========
```

### 慢请求警告
```
慢请求：POST /api/user/export 耗时 2500ms
```

## 安全特性

### 自动隐藏的敏感信息

#### 请求头
- Authorization
- Cookie
- Set-Cookie

#### 参数名（包含以下关键字）
- password, passwd, pwd
- token, secret, key
- credential, auth

## 性能考虑

1. **可配置开关**：可以通过 `request.log.enabled` 配置快速开启或关闭请求日志
2. **选择性记录**：可以选择只记录请求头或只记录请求参数
3. **生产环境建议**：在高并发生产环境中，建议关闭请求日志或降低日志级别，以避免影响性能

## 使用场景

1. **开发调试**：查看详细的请求和响应信息
2. **问题排查**：定位接口调用问题
3. **性能分析**：识别慢请求
4. **安全审计**：记录访问日志

## 注意事项

1. 请求日志会记录所有 HTTP 请求，可能产生大量日志，请注意日志文件大小
2. 敏感信息已自动隐藏，但仍需注意日志存储安全
3. 在生产环境中建议根据实际需要调整配置
4. 如需记录请求体内容，需要结合 `RepeatableFilter` 使用
