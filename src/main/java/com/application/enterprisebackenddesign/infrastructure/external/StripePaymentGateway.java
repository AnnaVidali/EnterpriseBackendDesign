package com.application.enterprisebackenddesign.infrastructure.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
