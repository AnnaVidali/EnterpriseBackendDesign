package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.CustomerUpdatedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.CrmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event handler: propagates customer profile changes to external CRM.
 *
 * DDD domain event pattern: Reacts to CustomerUpdatedEvent by syncing the
 * changed field to the CRM. This keeps the CRM eventually consistent with
 * the customer data without coupling the customer update use case to the
 * CRM API.
 */
@Component
public class CustomerUpdatedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomerUpdatedEventHandler.class);
    private final CrmService crmService;

    public CustomerUpdatedEventHandler(CrmService crmService) {
        this.crmService = crmService;
    }

    @Async
    @EventListener
    public void handle(CustomerUpdatedEvent event) {
        log.info("Handling customer updated event: id={}, field={}, old={}, new={}",
                event.getId(), event.getFieldName(), event.getOldValue(), event.getNewValue());
        crmService.syncCustomer(event.getId(), event.getFieldName(), event.getNewValue());
    }
}
