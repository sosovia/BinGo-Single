package com.bingo.common.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * RequestId 工具类
 * <p>
 * 用于生成、获取和管理请求追踪 ID，支持 MDC 日志上下文
 * </p>
 *
 */
@Slf4j
public class RequestIdUtils {

    /**
     * RequestId 请求追踪标识头
     */
    public static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * RequestId MDC Key
     */
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    private RequestIdUtils() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    /**
     * 生成新的 RequestId
     *
     * @return 新的 RequestId
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 从 MDC 获取 RequestId
     *
     * @return RequestId，如果不存在则返回 null
     */
    public static String getFromMDC() {
        return MDC.get(REQUEST_ID_MDC_KEY);
    }

    /**
     * 设置 RequestId 到 MDC
     *
     * @param requestId RequestId
     */
    public static void setToMDC(String requestId) {
        if (requestId != null && !requestId.trim().isEmpty()) {
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
        } else {
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    /**
     * 清除 MDC 中的 RequestId
     */
    public static void clearMDC() {
        MDC.remove(REQUEST_ID_MDC_KEY);
    }

    /**
     * 获取当前 RequestId（优先从 MDC 获取，否则生成新的）
     *
     * @return RequestId
     */
    public static String getCurrent() {
        String requestId = getFromMDC();
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = generate();
            setToMDC(requestId);
        }
        return requestId;
    }

    /**
     * 包装 Runnable，使其在异步线程中也能传递 RequestId
     *
     * @param runnable 原始 Runnable
     * @return 包装后的 Runnable
     */
    public static Runnable wrap(Runnable runnable) {
        String requestId = getCurrent();
        return () -> {
            try {
                setToMDC(requestId);
                runnable.run();
            } finally {
                clearMDC();
            }
        };
    }

    /**
     * 包装 Callable，使其在异步线程中也能传递 RequestId
     *
     * @param callable 原始 Callable
     * @return 包装后的 Callable
     */
    public static <T> Callable<T> wrap(Callable<T> callable) {
        String requestId = getCurrent();
        return () -> {
            try {
                setToMDC(requestId);
                return callable.call();
            } finally {
                clearMDC();
            }
        };
    }

    /**
     * 包装 Supplier，使其在异步线程中也能传递 RequestId
     *
     * @param supplier 原始 Supplier
     * @return 包装后的 Supplier
     */
    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        String requestId = getCurrent();
        return () -> {
            try {
                setToMDC(requestId);
                return supplier.get();
            } finally {
                clearMDC();
            }
        };
    }
}
