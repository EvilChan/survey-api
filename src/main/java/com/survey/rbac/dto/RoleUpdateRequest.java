package com.survey.rbac.dto;

public record RoleUpdateRequest(
        Long id,
        String code,
        String name,
        String description,
        Integer sort
) {
}
