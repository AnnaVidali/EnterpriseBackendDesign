package com.application.enterprisebackenddesign.api.customer;

public record CustomerResponse (
        Long id,
        String name,
        String lastName,
        String email
){}