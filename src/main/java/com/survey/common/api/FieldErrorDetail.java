package com.survey.common.api;

public record FieldErrorDetail(
    String field,
    Object rejected,
    String reason
) {}
