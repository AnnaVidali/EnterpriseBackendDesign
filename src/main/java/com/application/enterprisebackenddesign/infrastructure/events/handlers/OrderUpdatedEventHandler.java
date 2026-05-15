package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.OrderLineUpdatedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.CrmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event handler: propagates order line changes to the CRM.
 *
 * DDD domain event pattern: Handles OrderLineUpdatedEvent (published when
 * order line quantities change). The handler updates the CRM with the
 * new order status. This keeps the CRM eventually consistent without
 * requiring synchronous calls during order modification.
 */
@Component
public class OrderUpdatedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(OrderUpdatedEventHandler.class);
    private final CrmService crmService;

    public OrderUpdatedEventHandler(CrmService crmService) {
        this.crmService = crmService;
    }

    @Async
    @EventListener
    public void handle(OrderLineUpdatedEvent event) {
        log.info("Handling order updated event: orderId={}, lineId={}, oldQty={}, newQty={}",
                event.getOrderId(), event.getOrderLineId(), event.getOldQuantity(), event.getNewQuantity());
        crmService.updateOrderStatus(event.getOrderId(), "UPDATED");
    }
}
