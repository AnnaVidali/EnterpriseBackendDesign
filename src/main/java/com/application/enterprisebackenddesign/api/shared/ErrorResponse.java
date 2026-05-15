package com.application.enterprisebackenddesign.api.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<FieldErrorDetail> fieldErrors
) {

    public ErrorResponse(int status, String error, String message, String path, Instant timestamp) {
        this(status, error, message, path, timestamp, null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FieldErrorDetail(
            String field,
            String rejectedValue,
            String reason
    ) {}
}
