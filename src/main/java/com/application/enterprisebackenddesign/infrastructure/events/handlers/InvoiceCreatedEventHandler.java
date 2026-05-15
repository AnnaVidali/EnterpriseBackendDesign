package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.InvoiceCreatedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event handler: notifies accounting when an invoice is created.
 *
 * DDD domain event pattern: The InvoiceCreatedEvent is published by
 * IssueInvoiceUseCase after a confirmed order is billed. This handler
 * sends an email notification to the accounting department as a
 * fire-and-forget side effect.
 */
@Component
public class InvoiceCreatedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(InvoiceCreatedEventHandler.class);
    private final EmailService emailService;

    public InvoiceCreatedEventHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handle(InvoiceCreatedEvent event) {
        log.info("Handling invoice created event: id={}, orderId={}, amount={}",
                event.getId(), event.getOrderId(), event.getAmount());
        emailService.sendEmail(
                "accounting@example.com",
                "Invoice Created",
                "Invoice " + event.getId() + " for order " + event.getOrderId() + " has been created."
        );
    }
}
