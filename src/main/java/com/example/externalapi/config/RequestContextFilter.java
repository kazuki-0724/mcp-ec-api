package com.example.externalapi.config;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component("apiRequestContextFilter")
public class RequestContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = headerOrDefault(request.getHeader(CORRELATION_ID_HEADER), UUID.randomUUID().toString());
        String userId = headerOrDefault(request.getHeader(USER_ID_HEADER), "anonymous");
        long startTime = System.currentTimeMillis();

        MDC.put("correlationId", correlationId);
        MDC.put("userId", userId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        log.info(
                "http request method={} path={} query={} correlationId={} userId={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                correlationId,
                userId
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            log.info(
                    "http response method={} path={} status={} durationMs={} correlationId={} userId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    correlationId,
                    userId
            );
            MDC.clear();
        }
    }

    private String headerOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
