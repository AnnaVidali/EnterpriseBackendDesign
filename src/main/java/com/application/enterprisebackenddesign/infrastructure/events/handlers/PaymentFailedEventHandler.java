package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.PaymentFailedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PaymentFailedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(PaymentFailedEventHandler.class);
    private final EmailService emailService;

    public PaymentFailedEventHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handle(PaymentFailedEvent event) {
        log.info("Handling payment failed event: paymentId={}, invoiceId={}", event.getPaymentId(), event.getInvoiceId());
        emailService.sendEmail(
                "customer@example.com",
                "Payment Failed",
                "Your payment for invoice " + event.getInvoiceId() + " has failed. Reason: " + event.getReason()
        );
        emailService.sendEmail(
                "admin@example.com",
                "Payment Alert",
                "Payment " + event.getPaymentId() + " failed. Reason: " + event.getReason()
        );
    }
}
