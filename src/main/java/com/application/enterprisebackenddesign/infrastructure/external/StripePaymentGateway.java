package com.application.enterprisebackenddesign.infrastructure.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Concrete adapter for the PaymentGateway port — the Anti-Corruption Layer.
 *
 * Hexagonal Architecture: This class implements the PaymentGateway interface
 * (a "port" defined by the domain/application layer). It encapsulates all
 * knowledge of the Stripe SDK and HTTP/messaging protocols. The domain layer
 * never imports anything from com.stripe — it depends only on the PaymentGateway
 * interface and the simple GatewayPaymentRequest/PaymentResult DTOs.
 *
 * Anti-Corruption Layer (ACL) pattern: If Stripe changes its API or we switch
 * to a different provider (Adyen, Braintree, etc.), only this class changes.
 * The domain payment logic and ProcessPaymentUseCase remain untouched.
 *
 * Currently this is a stub implementation (returns success or "not configured").
 * In production it would call the Stripe API. The architectural pattern is the
 * same either way — the port/adapter boundary insulates the domain from the
 * external system.
 */
@Component
public class StripePaymentGateway implements PaymentGateway {

    private final String apiKey;

    public StripePaymentGateway(@Value("${payment.gateway.stripe.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public PaymentResult process(GatewayPaymentRequest request) {
        if (apiKey == null || apiKey.isBlank() || "sk_test_dummy".equals(apiKey)) {
            return new PaymentResult(false, null, "Payment gateway not configured");
        }

        try {
            String transactionId = "txn_" + System.currentTimeMillis();
            return new PaymentResult(true, transactionId, "Payment successful");
        } catch (Exception e) {
            return new PaymentResult(false, null, "Payment failed: " + e.getMessage());
        }
    }
}
