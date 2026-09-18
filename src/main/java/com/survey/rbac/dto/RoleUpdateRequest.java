package com.survey.rbac.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(
        @NotNull Long id,
        @Size(max = 64) String code,
        @Size(max = 64) String name,
        @Size(max = 255) String description,
        Integer sort
) {
}
