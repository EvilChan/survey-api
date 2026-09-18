package com.survey.common.error;

import java.util.List;

import com.survey.common.api.FieldErrorDetail;

public class BizException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<FieldErrorDetail> details;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public BizException(ErrorCode errorCode, List<FieldErrorDetail> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<FieldErrorDetail> getDetails() {
        return details;
    }

}
