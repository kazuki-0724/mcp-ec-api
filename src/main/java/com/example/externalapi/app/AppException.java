package com.example.externalapi.app;

import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final ErrorCode code;
    private final HttpStatus status;
    private final ErrorType errorType;

    public AppException(ErrorCode code, String message, HttpStatus status, ErrorType errorType) {
        super(message);
        this.code = code;
        this.status = status;
        this.errorType = errorType;
    }

    public ErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public static AppException badUserInput(String message) {
        return new AppException(ErrorCode.BAD_USER_INPUT, message, HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST);
    }

    public static AppException notFound(String message) {
        return new AppException(ErrorCode.NOT_FOUND, message, HttpStatus.NOT_FOUND, ErrorType.NOT_FOUND);
    }

    public static AppException upstreamTimeout(String message) {
        return new AppException(ErrorCode.UPSTREAM_TIMEOUT, message, HttpStatus.GATEWAY_TIMEOUT, ErrorType.INTERNAL_ERROR);
    }

    public static AppException internalError(String message) {
        return new AppException(ErrorCode.INTERNAL_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL_ERROR);
    }
}
