package com.bingo.common.web.filter;

import com.bingo.common.web.config.properties.RequestLogProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static com.bingo.common.core.utils.RequestIdUtils.REQUEST_ID_MDC_KEY;

/**
 * 请求日志过滤器
 * <p>
 * 用于记录所有 HTTP 请求的详细信息，包括请求方法、URL、参数、头部信息等
 * </p>
 *
 */
@Slf4j
@RequiredArgsConstructor
public class RequestLogFilter implements Filter, Ordered {

    private final RequestLogProperties requestLogProperties;

    /**
     * 需要隐藏的请求头参数
     */
    private static final Set<String> HIDDEN_HEADERS = new HashSet<>(Arrays.asList(
        "Authorization",
        "Cookie",
        "Set-Cookie"
    ));

    /**
     * 需要隐藏的参数名关键字
     */
    private static final Set<String> HIDDEN_PARAM_KEYWORDS = new HashSet<>(Arrays.asList(
        "password", "passwd", "pwd",
        "token", "secret", "key",
        "credential", "auth"
    ));

    /**
     * 过滤器初始化入口
     *
     * @param filterConfig 过滤器配置对象
     * @throws ServletException 过滤器初始化异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("请求日志过滤器已初始化");
    }

    /**
     * 过滤请求，记录请求和响应的详细信息
     *
     * @param request  Servlet 请求对象
     * @param response Servlet 响应对象
     * @param chain    过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        // 检查是否启用请求日志
        if (!requestLogProperties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        if (!(request instanceof HttpServletRequest httpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // 记录请求信息
        logRequest(httpServletRequest);

        long startTime = System.currentTimeMillis();

        try {
            // 执行请求
            chain.doFilter(request, response);

            // 记录响应信息
            long duration = System.currentTimeMillis() - startTime;
            logResponse(httpServletResponse, duration);

            // 检查是否为慢请求
            checkSlowRequest(httpServletRequest, duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            String requestId = MDC.get(REQUEST_ID_MDC_KEY);
            log.error("请求处理异常：[{}] {} {} - 耗时 {}ms",
                requestId != null ? requestId : "N/A",
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI(),
                duration, e);
            throw e;
        }
    }

    /**
     * 记录请求信息
     *
     * @param request HTTP 请求对象
     */
    private void logRequest(HttpServletRequest request) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n========== 请求开始 ==========\n");
        logMessage.append("请求方法：").append(request.getMethod()).append("\n");
        logMessage.append("请求 URL：").append(request.getRequestURL()).append("\n");
        logMessage.append("请求 URI：").append(request.getRequestURI()).append("\n");
        logMessage.append("上下文路径：").append(request.getContextPath()).append("\n");
        logMessage.append("查询参数：").append(request.getQueryString() != null ? request.getQueryString() : "无").append("\n");
        logMessage.append("远程地址：").append(request.getRemoteAddr()).append("\n");
        logMessage.append("远程主机：").append(request.getRemoteHost()).append("\n");
        logMessage.append("协议：").append(request.getProtocol()).append("\n");
        logMessage.append("方案：").append(request.getScheme()).append("\n");
        logMessage.append("是否 HTTPS：").append(request.isSecure() ? "是" : "否").append("\n");

        // 添加 RequestId
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        if (requestId != null && !requestId.trim().isEmpty()) {
            logMessage.append("追踪 ID：").append(requestId).append("\n");
        }

        // 记录请求头
        if (requestLogProperties.isLogHeaders()) {
            logMessage.append("请求头:\n");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                // 隐藏敏感信息
                if (requestLogProperties.isHideSensitiveData() && HIDDEN_HEADERS.contains(headerName)) {
                    headerValue = "***";
                }
                logMessage.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
            }
        }

        // 记录请求参数
        if (requestLogProperties.isLogParameters()) {
            logMessage.append("请求参数:\n");
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                // 隐藏敏感参数
                if (requestLogProperties.isHideSensitiveData() && containsSensitiveKeyword(paramName)) {
                    logMessage.append("  ").append(paramName).append(": ***\n");
                } else {
                    logMessage.append("  ").append(paramName).append(": ")
                        .append(Arrays.toString(paramValues)).append("\n");
                }
            }
        }

        logMessage.append("========== 请求结束 ==========");
        log.info(logMessage.toString());
    }

    /**
     * 记录响应信息
     *
     * @param response HTTP 响应对象
     * @param duration 请求处理耗时（毫秒）
     */
    private void logResponse(HttpServletResponse response, long duration) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n========== 响应开始 ==========\n");
        logMessage.append("状态码：").append(response.getStatus()).append("\n");
        logMessage.append("内容类型：").append(response.getContentType() != null ? response.getContentType() : "未设置").append("\n");
//        logMessage.append("内容长度：").append(response.getContentLengthLong() >= 0 ? response.getContentLengthLong() : "未知").append("\n");
        logMessage.append("处理耗时:").append(duration).append("ms\n");

        // 添加 RequestId
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        if (requestId != null && !requestId.trim().isEmpty()) {
            logMessage.append("追踪 ID:").append(requestId).append("\n");
        }

        logMessage.append("========== 响应结束 ==========");
        log.info(logMessage.toString());
    }

    /**
     * 检查是否为慢请求
     *
     * @param request  HTTP 请求对象
     * @param duration 请求处理耗时（毫秒）
     */
    private void checkSlowRequest(HttpServletRequest request, long duration) {
        if (duration > requestLogProperties.getSlowThreshold()) {
            log.warn("慢请求：{} {} 耗时 {}ms",
                request.getMethod(),
                request.getRequestURI(),
                duration);
        }
    }

    /**
     * 检查参数名是否包含敏感关键字
     *
     * @param paramName 参数名
     * @return 是否包含敏感关键字
     */
    private boolean containsSensitiveKeyword(String paramName) {
        String lowerParamName = paramName.toLowerCase();
        return HIDDEN_PARAM_KEYWORDS.stream()
            .anyMatch(lowerParamName::contains);
    }

    /**
     * 过滤器销毁入口，释放资源
     */
    @Override
    public void destroy() {
        log.info("请求日志过滤器已销毁");
    }

    /**
     * 设置过滤器顺序，确保在其他过滤器之前执行
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
