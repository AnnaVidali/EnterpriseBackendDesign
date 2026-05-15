package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.InvoiceIssuedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event handler: sends invoice notification to the customer.
 *
 * DDD domain event pattern: Handles InvoiceIssuedEvent (published after
 * an invoice transitions to ISSUED status). This handler sends the
 * invoice to the customer — a bounded context boundary where the billing
 * system notifies the customer communication system.
 *
 * The customer email is hardcoded here (stub). In production it would
 * be looked up from the Customer aggregate using the customerId.
 */
@Component
public class InvoiceIssuedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(InvoiceIssuedEventHandler.class);
    private final EmailService emailService;

    public InvoiceIssuedEventHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handle(InvoiceIssuedEvent event) {
        log.info("Handling invoice issued event: invoiceId={}, customerId={}", event.getInvoiceId(), event.getCustomerId());
        emailService.sendEmail(
                "customer@example.com",
                "Invoice Issued",
                "Your invoice " + event.getInvoiceId() + " for amount " + event.getAmount() + " has been issued."
        );
    }
}
