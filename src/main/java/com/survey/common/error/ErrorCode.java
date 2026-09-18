package com.survey.common.error;

public enum ErrorCode {
    SUCCESS("0", "OK", "操作成功", 200),
    // 通用错误 C0001-C9999
    /** C0001-参数错误 */
    PARAM_INVALID("C0001", "PARAM_INVALID", "参数错误", 400),
    // 业务错误 B0001-B9999 — rbac/role
    /** 角色不存在或已删除 */
    ROLE_NOT_FOUND("B0001", "ROLE_NOT_FOUND", "角色不存在或已删除", 404),
    /** 角色编码与未删除记录冲突 */
    ROLE_CODE_DUPLICATE("B0002", "ROLE_CODE_DUPLICATE", "角色编码已存在", 409),
    /** 内置角色禁止修改编码 */
    ROLE_BUILTIN_CODE_IMMUTABLE("B0003", "ROLE_BUILTIN_CODE_IMMUTABLE", "内置角色禁止修改编码", 400),
    /** 内置角色禁止删除 */
    ROLE_BUILTIN_DELETE_FORBIDDEN("B0004", "ROLE_BUILTIN_DELETE_FORBIDDEN", "内置角色禁止删除", 400),
    /** 内置角色禁止停用 */
    ROLE_BUILTIN_DISABLE_FORBIDDEN("B0005", "ROLE_BUILTIN_DISABLE_FORBIDDEN", "内置角色禁止停用", 400),
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
