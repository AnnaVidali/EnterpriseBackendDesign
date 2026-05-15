package com.application.enterprisebackenddesign.infrastructure.external;

/**
 * Abstraction for external payment processing.
 *
 * Interview context: This interface is the "anti-corruption layer" between
 * our domain and the external payment provider (e.g., Stripe, Braintree).
 * The domain and application layers depend only on this interface — they
 * never import Stripe SDK classes.
 *
 * Benefits:
 * 1. Testability: In unit tests, we mock PaymentGateway to simulate
 *    success/failure without calling real payment APIs.
 * 2. Swapability: We can change payment providers without touching
 *    any domain or application code — just write a new implementation.
 * 3. Isolation: If Stripe's API changes or has a temporary outage,
 *    our code only needs changes in one place.
 */
public interface PaymentGateway {

    PaymentResult process(GatewayPaymentRequest request);
}
