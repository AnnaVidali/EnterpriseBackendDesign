package com.application.enterprisebackenddesign.api.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        @NotBlank String currency,
        @Valid List<OrderLineRequest> lines
) {}
