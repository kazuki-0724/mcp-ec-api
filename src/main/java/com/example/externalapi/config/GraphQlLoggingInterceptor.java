package com.example.externalapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;

import reactor.core.publisher.Mono;

@Component
public class GraphQlLoggingInterceptor implements WebGraphQlInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GraphQlLoggingInterceptor.class);

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String operationName = request.getOperationName() == null ? "anonymous" : request.getOperationName();
        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");

        log.info("graphql request operationName={} correlationId={} userId={}", operationName, correlationId, userId);

        return chain.next(request)
                .doOnNext(response -> log.info(
                        "graphql response operationName={} correlationId={} errors={}",
                        operationName,
                        correlationId,
                        response.getErrors().size()
                ));
    }
}
