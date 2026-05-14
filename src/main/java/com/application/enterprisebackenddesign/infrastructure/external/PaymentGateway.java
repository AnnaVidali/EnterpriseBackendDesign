package com.application.enterprisebackenddesign.infrastructure.external;

public interface PaymentGateway {

    PaymentResult process(GatewayPaymentRequest request);
}
