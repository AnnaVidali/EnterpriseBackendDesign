package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.customer.CustomerRepository;
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
    private final CustomerRepository customerRepository;

    public PaymentCompletedEventHandler(EmailService emailService, CrmService crmService, CustomerRepository customerRepository) {
        this.emailService = emailService;
        this.crmService = crmService;
        this.customerRepository = customerRepository;
    }

    @Async
    @EventListener
    public void handle(PaymentCompletedEvent event) {
        log.info("Handling payment completed event: paymentId={}, orderId={}", event.getPaymentId(), event.getOrderId());
        var customer = customerRepository.findById(event.getCustomerId()).orElse(null);
        if (customer == null) {
            log.warn("Customer not found for id={}, skipping email", event.getCustomerId());
        } else {
            emailService.sendEmail(
                    customer.getEmail(),
                    "Payment Confirmed",
                    "Your payment of " + event.getAmount() + " has been confirmed."
            );
        }
        crmService.updateOrderStatus(event.getOrderId(), "PAID");
    }
}
