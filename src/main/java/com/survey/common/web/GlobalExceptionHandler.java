package com.survey.common.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.survey.common.api.ApiResult;
import com.survey.common.api.FieldErrorDetail;
import com.survey.common.error.BizException;
import com.survey.common.error.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResult<Void>> handleBizException(BizException e) {
        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ApiResult.fail(e.getErrorCode(), e.getMessage(), e.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
                .map(err -> new FieldErrorDetail(err.getField(), err.getRejectedValue(), err.getDefaultMessage()))
                .toList();
        return ResponseEntity
            .status(ErrorCode.PARAM_INVALID.getHttpStatus())
            .body(ApiResult.fail(ErrorCode.PARAM_INVALID, ErrorCode.PARAM_INVALID.getMessage(), details));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResult<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e) {
        List<FieldErrorDetail> details = List.of(
                new FieldErrorDetail(e.getParameterName(), null, e.getMessage()));
        return ResponseEntity
            .status(ErrorCode.PARAM_INVALID.getHttpStatus())
            .body(ApiResult.fail(ErrorCode.PARAM_INVALID, ErrorCode.PARAM_INVALID.getMessage(), details));
    }
}
