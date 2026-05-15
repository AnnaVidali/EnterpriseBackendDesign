package com.application.enterprisebackenddesign.infrastructure.events.handlers;

import com.application.enterprisebackenddesign.domain.shared.PaymentCompletedEvent;
import com.application.enterprisebackenddesign.infrastructure.service.CrmService;
import com.application.enterprisebackenddesign.infrastructure.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles payment completion by sending confirmation and updating CRM.
 *
 * Interview context: This is an async event handler. When a payment is
 * successfully processed, the ProcessPaymentUseCase publishes a
 * PaymentCompletedEvent via Spring's ApplicationEventPublisher. This
 * handler picks it up and triggers side effects:
 * 1. Email confirmation to the customer
 * 2. CRM status update to "PAID"
 *
 * The @Async annotation means these side effects run in a separate thread.
 * The API response returns to the client immediately without waiting for
 * the email to send or the CRM to update. If the email service is down,
 * the payment is still recorded as completed — the side effect failure
 * doesn't roll back the transaction.
 *
 * This separation of concerns (main flow in use case, side effects in
 * handlers) keeps the use case clean and the handlers independently
 * testable. See EventHandlersTest for unit tests with mocked services.
 */
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
