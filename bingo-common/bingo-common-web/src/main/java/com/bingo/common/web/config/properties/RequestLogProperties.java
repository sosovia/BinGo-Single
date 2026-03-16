package com.bingo.common.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 请求日志配置属性
 */
@Data
@ConfigurationProperties(prefix = "request.log")
public class RequestLogProperties {

    /**
     * 是否启用请求日志，默认启用
     */
    private boolean enabled = true;

    /**
     * 是否记录请求头，默认记录
     */
    private boolean logHeaders = true;

    /**
     * 是否记录请求参数，默认记录
     */
    private boolean logParameters = true;

    /**
     * 是否隐藏敏感信息，默认隐藏
     */
    private boolean hideSensitiveData = true;

    /**
     * 慢请求阈值（毫秒），超过此阈值的请求会被标记为慢请求
     */
    private long slowThreshold = 1000L;
}
