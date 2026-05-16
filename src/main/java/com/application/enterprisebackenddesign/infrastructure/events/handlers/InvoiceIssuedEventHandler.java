package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
import com.application.enterprisebackenddesign.domain.shared.InvoiceIssuedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class InvoiceIssuedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(InvoiceIssuedEventHandler.class);
    private final EmailService emailService;
    private final CustomerRepository customerRepository;

    public InvoiceIssuedEventHandler(EmailService emailService, CustomerRepository customerRepository) {
        this.emailService = emailService;
        this.customerRepository = customerRepository;
    }

    @Async
    @EventListener
    public void handle(InvoiceIssuedEvent event) {
        log.info("Handling invoice issued event: invoiceId={}, customerId={}", event.getInvoiceId(), event.getCustomerId());
        var customer = customerRepository.findById(event.getCustomerId()).orElse(null);
        if (customer == null) {
            log.warn("Customer not found for id={}, skipping email", event.getCustomerId());
            return;
        }
        emailService.sendEmail(
                customer.getEmail(),
                "Invoice Issued",
                "Your invoice " + event.getInvoiceId() + " for amount " + event.getAmount() + " has been issued."
        );
    }
}
