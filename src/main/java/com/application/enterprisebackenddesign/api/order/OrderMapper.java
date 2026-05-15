package com.application.enterprisebackenddesign.api.order;

import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.stream.Collectors;

/**
 * API-layer mapper for the Order aggregate.
 *
 * Boundary translation (Hexagonal Architecture): This mapper converts between
 * API DTOs (OrderLineRequest/OrderLineResponse) and domain objects (OrderLine).
 * It sits at the outermost adapter layer and handles:
 * - Dismantling Money construction from separate amount + currency fields
 * - Wrapping domain BusinessRuleViolationExceptions into runtime
 *   IllegalArgumentExceptions (HTTP boundary translation)
 *
 * The separation between this API mapper and the infrastructure/persistence/order/OrderMapper
 * is intentional: one handles HTTP concerns, the other handles persistence concerns.
 * They are in different hexagon layers and should not be confused.
 */
@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getCurrency().getCurrencyCode(),
                order.getTotalAmount().getAmount(),
                order.getOrderLines().stream().map(this::toLineResponse).collect(Collectors.toList())
        );
    }

    private OrderLineResponse toLineResponse(OrderLine line) {
        return new OrderLineResponse(
                line.getId(),
                line.getProductId(),
                line.getQuantity(),
                line.getPrice().getAmount()
        );
    }

    public OrderLine toOrderLine(Long orderLineId, OrderLineRequest request, Currency currency) {
        try {
            Money price = new Money(request.price(), currency);
            return new OrderLine(orderLineId, request.productId(), request.quantity(), price);
        } catch (DomainException.BusinessRuleViolationException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
