package com.application.enterprisebackenddesign.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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
 * This implementation calls the Stripe API over HTTP. The API URL is
 * configurable via the `payment.gateway.stripe.api-url` property, which
 * allows WireMock-based integration tests to inject a local server URL.
 */
@Component
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    private final String apiKey;
    private final String apiUrl;
    private final RestTemplate restTemplate;

    public StripePaymentGateway(
            @Value("${payment.gateway.stripe.api-key}") String apiKey,
            @Value("${payment.gateway.stripe.api-url:https://api.stripe.com/v1/charges}") String apiUrl,
            RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public PaymentResult process(GatewayPaymentRequest request) {
        if (apiKey == null || apiKey.isBlank() || "sk_test_dummy".equals(apiKey)) {
            return new PaymentResult(false, null, "Payment gateway not configured");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "amount", request.amount().getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue(),
                    "currency", request.amount().getCurrency().getCurrencyCode().toLowerCase(),
                    "description", "Invoice " + request.invoiceId()
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            String transactionId = (String) responseBody.get("id");
            log.info("Stripe charge created: {}", transactionId);
            return new PaymentResult(true, transactionId, "Payment successful");
        } catch (HttpClientErrorException e) {
            log.warn("Stripe API error: {}", e.getResponseBodyAsString());
            return new PaymentResult(false, null, "Payment failed: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Stripe payment failed", e);
            return new PaymentResult(false, null, "Payment failed: " + e.getMessage());
        }
    }
}
