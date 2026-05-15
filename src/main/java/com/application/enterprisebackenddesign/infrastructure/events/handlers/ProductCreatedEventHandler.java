package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.ProductCreatedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event handler: syncs new products to inventory and warehouse.
 *
 * DDD domain event pattern: ProductCreatedEvent is published when a new
 * product is added to the catalog. This handler updates the inventory
 * system and notifies the warehouse — two independent downstream systems
 * that need to know about the new product.
 */
@Component
public class ProductCreatedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(ProductCreatedEventHandler.class);
    private final InventoryService inventoryService;

    public ProductCreatedEventHandler(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Async
    @EventListener
    public void handle(ProductCreatedEvent event) {
        log.info("Handling product created event: id={}, sku={}", event.getId(), event.getSku());
        inventoryService.updateProduct(event.getSku(), event.getName());
        inventoryService.notifyWarehouse(event.getId());
    }
}
