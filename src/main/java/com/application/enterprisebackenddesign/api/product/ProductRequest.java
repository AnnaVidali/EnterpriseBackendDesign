package com.application.enterprisebackenddesign.api.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest (

    @NotNull @NotBlank String name,
    @NotNull @NotBlank @Positive BigDecimal price,
    @NotNull @NotBlank String currency,
    @NotNull @NotBlank String sku

){}
