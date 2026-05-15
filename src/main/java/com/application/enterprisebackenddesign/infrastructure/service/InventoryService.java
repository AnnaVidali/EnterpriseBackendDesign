package com.application.enterprisebackenddesign.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    public void notifyWarehouse(Long orderId) {
        log.info("Notifying warehouse for order: orderId={}", orderId);
    }

    public void updateProduct(String sku, String name) {
        log.info("Updating product in inventory system: sku={}, name={}", sku, name);
    }
}
