package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.ProductUpdatedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event handler: propagates product changes to the inventory system.
 *
 * DDD domain event pattern: ProductUpdatedEvent is published when product
 * details change. This handler pushes the updated field to the inventory
 * system, maintaining eventual consistency between catalog and inventory
 * bounded contexts.
 */
@Component
public class ProductUpdatedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(ProductUpdatedEventHandler.class);
    private final InventoryService inventoryService;

    public ProductUpdatedEventHandler(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Async
    @EventListener
    public void handle(ProductUpdatedEvent event) {
        log.info("Handling product updated event: id={}, field={}", event.getId(), event.getFieldName());
        inventoryService.updateProduct(event.getFieldName(), String.valueOf(event.getNewValue()));
    }
}
