package com.application.enterprisebackenddesign.api.auth;

public record LoginResponse(
        String token,
        String username
) {
}
