package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.OrderCreatedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.AnalyticsService;
import com.application.enterprisebackenddesign.infrastructure.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event handler: triggers analytics tracking and warehouse notification.
 *
 * DDD domain event pattern: OrderCreatedEvent is published when a new order
 * is placed. This handler fires two independent side effects asynchronously:
 * analytics tracking (business intelligence) and warehouse notification
 * (inventory management). These are different bounded contexts that both
 * consume the same event.
 */
@Component
public class OrderCreatedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventHandler.class);
    private final AnalyticsService analyticsService;
    private final InventoryService inventoryService;

    public OrderCreatedEventHandler(AnalyticsService analyticsService, InventoryService inventoryService) {
        this.analyticsService = analyticsService;
        this.inventoryService = inventoryService;
    }

    @Async
    @EventListener
    public void handle(OrderCreatedEvent event) {
        log.info("Handling order created event: orderId={}, customerId={}", event.getOrderId(), event.getCustomerId());
        analyticsService.trackOrderCreated(event.getOrderId(), event.getCustomerId(), event.getLineCount());
        inventoryService.notifyWarehouse(event.getOrderId());
    }
}
