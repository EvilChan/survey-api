package com.survey.rbac.dto;

import java.time.Instant;

public record RoleResponse(
        Long id,
        String code,
        String name,
        String description,
        Boolean builtin,
        Integer status,
        Integer sort,
        Instant createdAt,
        Instant updatedAt
) {
}
