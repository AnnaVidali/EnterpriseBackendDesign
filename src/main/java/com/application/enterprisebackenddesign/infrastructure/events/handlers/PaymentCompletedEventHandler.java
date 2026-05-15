package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.PaymentCompletedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.CrmService;
import com.application.enterprisebackenddesign.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedEventHandler {
    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedEventHandler.class);
    private final EmailService emailService;
    private final CrmService crmService;

    public PaymentCompletedEventHandler(EmailService emailService, CrmService crmService) {
        this.emailService = emailService;
        this.crmService = crmService;
    }

    @Async
    @EventListener
    public void handle(PaymentCompletedEvent event) {
        log.info("Handling payment completed event: paymentId={}, orderId={}", event.getPaymentId(), event.getOrderId());
        emailService.sendEmail(
                "customer@example.com",
                "Payment Confirmed",
                "Your payment of " + event.getAmount() + " has been confirmed."
        );
        crmService.updateOrderStatus(event.getOrderId(), "PAID");
    }
}
