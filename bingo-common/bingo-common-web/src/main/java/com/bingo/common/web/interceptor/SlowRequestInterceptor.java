package com.bingo.common.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class SlowRequestInterceptor implements HandlerInterceptor {

    private static final long SLOW_THRESHOLD = 1000L; // 1秒
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        startTime.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        long duration = System.currentTimeMillis() - startTime.get();
        if (duration > SLOW_THRESHOLD) {
            log.warn("慢请求: {} {} 耗时 {}ms",
                request.getMethod(),
                request.getRequestURI(),
                duration);
        }
        startTime.remove();
    }
}
