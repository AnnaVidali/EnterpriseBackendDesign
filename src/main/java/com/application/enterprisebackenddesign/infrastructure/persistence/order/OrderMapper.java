package com.application.enterprisebackenddesign.infrastructure.persistence.order;

import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import com.application.enterprisebackenddesign.infrastructure.persistence.order.entity.OrderEntity;
import com.application.enterprisebackenddesign.infrastructure.persistence.order.entity.OrderLineEntity;
import com.application.enterprisebackenddesign.infrastructure.persistence.shared.MoneyEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Persistence-layer mapper between Order domain objects and OrderEntity JPA entities.
 *
 * This mapper implements the bidirectional transformation that makes the
 * persistence-ignorant domain model work with JPA. It is the bridge between
 * the domain's rich object graph and the relational entity model.
 *
 * The most critical detail is in toLineEntity(): the call to
 * entity.setOrder(parent) on line 62. For a bidirectional @OneToMany mapping,
 * JPA requires both sides of the relationship to be set. Without this,
 * the foreign key column (order_id) would be null when saving.
 *
 * Tradeoff: The mapper catches DomainException.BusinessRuleViolationException
 * from domain constructors and wraps it in RuntimeException. This is because
 * domain validation should have already happened before reaching persistence —
 * if it fails here, it indicates a bug upstream, not a recoverable condition.
 */
@Component("infraOrderMapper")
public class OrderMapper {

    public OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setCurrency(order.getCurrency().getCurrencyCode());
        entity.setTotalAmount(order.getTotalAmount().getAmount());

        List<OrderLineEntity> orderLineEntities = order.getOrderLines().stream()
                .map(line -> toLineEntity(line, entity))
                .collect(Collectors.toCollection(ArrayList::new));

        entity.setOrderLines(orderLineEntities);
        return entity;
    }

    public Order toDomain(OrderEntity entity) {
        List<OrderLine> orderLines = entity.getOrderLines() == null
                ? List.of()
                : entity.getOrderLines().stream()
                .map(this::toLineDomain)
                .toList();

        try {
            return new Order(
                    entity.getId(),
                    entity.getCustomerId(),
                    orderLines,
                    Currency.getInstance(entity.getCurrency()),
                    entity.getStatus()
            );
        } catch (DomainException.BusinessRuleViolationException e) {
            throw new RuntimeException("Failed to create Order from entity", e);
        }
    }

    private OrderLineEntity toLineEntity(OrderLine orderLine, OrderEntity parent) {
        OrderLineEntity entity = new OrderLineEntity();
        entity.setId(orderLine.getId());
        entity.setProductId(orderLine.getProductId());
        entity.setQuantity(orderLine.getQuantity());
        entity.setPrice(MoneyEmbeddable.fromDomain(orderLine.getPrice()));
        entity.setOrder(parent); // important for JPA
        return entity;
    }

    private OrderLine toLineDomain(OrderLineEntity entity) {
        try {
            Money price = entity.getPrice().toDomain();
            return new OrderLine(
                    entity.getId(),
                    entity.getProductId(),
                    entity.getQuantity(),
                    price
            );
        } catch (DomainException.BusinessRuleViolationException e) {
            throw new RuntimeException("Failed to create OrderLine from entity", e);
        }
    }
}
