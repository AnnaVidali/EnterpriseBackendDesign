package com.application.enterprisebackenddesign.api.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderLineRequest(
        @NotNull Long productId,
        @Min(1) int quantity,
        @NotNull BigDecimal price,
        String currency
) {}
