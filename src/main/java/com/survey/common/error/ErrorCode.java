package com.survey.common.error;

public enum ErrorCode {
    SUCCESS("0", "OK", "操作成功", 200),
    // 通用错误 C0001-C9999
    /** C0001-参数错误 */
    PARAM_INVALID("C0001", "PARAM_INVALID", "参数错误", 400),
    ;

    /** 对外错误码（字符串），永久不可变更 */
    private final String code;
    /** 英文标识符，全大写蛇形 */
    private final String identifier;
    /** 面向终端用户的文案 */
    private final String message;
    /** HTTP 状态码 */
    private final int httpStatus;

    ErrorCode(String code, String identifier, String message, int httpStatus) {
        this.code = code;
        this.identifier = identifier;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}
