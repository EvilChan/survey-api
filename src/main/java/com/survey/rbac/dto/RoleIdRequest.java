package com.survey.rbac.dto;

import jakarta.validation.constraints.NotNull;

public record RoleIdRequest(
        @NotNull Long id
) {
}
