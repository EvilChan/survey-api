package com.survey.rbac.dto;

public record RoleCreateRequest(
        String code,
        String name,
        String description,
        Integer sort
) {
}
