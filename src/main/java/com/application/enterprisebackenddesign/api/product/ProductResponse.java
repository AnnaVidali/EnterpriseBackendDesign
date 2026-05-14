package com.application.enterprisebackenddesign.api.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        String currency,
        String sku
) {}
