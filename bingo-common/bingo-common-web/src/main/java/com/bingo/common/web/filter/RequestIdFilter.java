package com.bingo.common.web.filter;

import com.bingo.common.core.utils.RequestIdUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static com.bingo.common.core.utils.RequestIdUtils.REQUEST_ID_HEADER;

/**
 * RequestId 过滤器
 * <p>
 * 在请求入口生成或提取 RequestId，并设置到 MDC 和响应头中，实现全链路追踪
 * </p>
 *
 */
@Slf4j
public class RequestIdFilter implements Filter, Ordered {

    /**
     * 过滤器初始化入口
     *
     * @param filterConfig 过滤器配置对象
     * @throws ServletException 过滤器初始化异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("RequestId 过滤器已初始化");
    }

    /**
     * 过滤请求，处理 RequestId
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
        if (!(request instanceof HttpServletRequest httpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // 1. 尝试从请求头获取 RequestId（如果前端传递）
        String requestId = httpServletRequest.getHeader(REQUEST_ID_HEADER);

        // 2. 如果请求头没有，则生成新的 RequestId
        if (!StringUtils.hasText(requestId)) {
            requestId = RequestIdUtils.generate();
        }

        // 3. 设置 RequestId 到 MDC
        RequestIdUtils.setToMDC(requestId);

        try {
            // 4. 执行请求
            chain.doFilter(request, response);
        } finally {
            // 5. 在响应头中添加 RequestId，便于客户端追踪
            httpServletResponse.setHeader(REQUEST_ID_HEADER, requestId);

            // 6. 清除 MDC（防止线程池复用导致的污染）
            RequestIdUtils.clearMDC();
        }
    }

    /**
     * 过滤器销毁入口，释放资源
     */
    @Override
    public void destroy() {
        log.info("RequestId 过滤器已销毁");
    }

    /**
     * 设置过滤器顺序，确保在其他过滤器之前执行（最高优先级）
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
