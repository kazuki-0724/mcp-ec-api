package com.example.externalapi.config;

import java.util.Map;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import com.example.externalapi.app.AppException;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        AppException appException = ex instanceof AppException existing
                ? existing
                : AppException.internalError("unexpected internal error");

        return GraphqlErrorBuilder.newError(env)
                .message(appException.getMessage())
                .errorType(appException.getErrorType())
                .extensions(Map.of("code", appException.getCode().name()))
                .build();
    }
}
