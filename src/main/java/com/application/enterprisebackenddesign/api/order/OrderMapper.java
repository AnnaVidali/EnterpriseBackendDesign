package com.application.enterprisebackenddesign.api.order;

import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.stream.Collectors;

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
