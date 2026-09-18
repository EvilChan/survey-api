package com.survey.rbac.dto;

public record RoleListRequest(
        Integer page,
        Integer size,
        Integer status,
        String keyword
) {
}
