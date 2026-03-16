package com.bingo.common.web.config;

import com.bingo.common.web.config.properties.RequestLogProperties;
import com.bingo.common.web.config.properties.XssProperties;
import com.bingo.common.web.filter.RepeatableFilter;
import com.bingo.common.web.filter.RequestIdFilter;
import com.bingo.common.web.filter.RequestLogFilter;
import com.bingo.common.web.filter.XssFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Filter 配置
 */
@AutoConfiguration
@EnableConfigurationProperties({XssProperties.class, RequestLogProperties.class})
public class FilterConfig {

    /**
     * 注册 XSS 过滤器。
     *
     * @return XSS 请求过滤器实例
     */
    @Bean
    @ConditionalOnProperty(value = "xss.enabled", havingValue = "true")
    @FilterRegistration(
        name = "xssFilter",
        urlPatterns = "/*",
        order = FilterRegistrationBean.HIGHEST_PRECEDENCE + 1,
        dispatcherTypes = DispatcherType.REQUEST
    )
    public XssFilter xssFilter() {
        return new XssFilter();
    }

    /**
     * 注册可重复读取请求体过滤器。
     *
     * @return 请求包装过滤器实例
     */
    @Bean
    @FilterRegistration(name = "repeatableFilter", urlPatterns = "/*")
    public RepeatableFilter repeatableFilter() {
        return new RepeatableFilter();
    }

    /**
     * 注册请求日志过滤器。
     *
     * @param requestLogProperties 请求日志配置属性
     * @return 请求日志过滤器实例
     */
    @Bean
    @FilterRegistration(
        name = "requestLogFilter",
        urlPatterns = "/*",
        order = FilterRegistrationBean.HIGHEST_PRECEDENCE + 10
    )
    public RequestLogFilter requestLogFilter(RequestLogProperties requestLogProperties) {
        return new RequestLogFilter(requestLogProperties);
    }

    /**
     * 注册请求 ID 过滤器。
     *
     * @return 请求 ID 过滤器实例
     */
    @Bean
    @FilterRegistration(name = "requestIdFilter", urlPatterns = "/*")
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }

}
