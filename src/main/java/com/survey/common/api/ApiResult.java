package com.survey.common.api;

import java.time.Instant;
import java.util.List;

import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.survey.common.error.ErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(
    String code,
    String message,
    T data,
    List<FieldErrorDetail> details,
    String traceId,
    Instant timestamp
) {

    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(
            ErrorCode.SUCCESS.getCode(),
            ErrorCode.SUCCESS.getMessage(),
            null,
            null,
            null,
            Instant.now()
        );
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(
            ErrorCode.SUCCESS.getCode(),
            ErrorCode.SUCCESS.getMessage(),
            data,
            null,
            null,
            Instant.now()
        );
    }

    public static <T> ApiResult<T> fail(ErrorCode errorCode, String message, List<FieldErrorDetail> details) {
        return new ApiResult<>(
            errorCode.getCode(),
            message != null ? message : errorCode.getMessage(),
            null,
            details,
            MDC.get("traceId"),
            Instant.now()
        );
    }

}
