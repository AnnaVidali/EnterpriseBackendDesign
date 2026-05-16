package com.application.enterprisebackenddesign.infrastructure.external;

import com.application.enterprisebackenddesign.domain.shared.Money;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Currency;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class StripePaymentGatewayIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension.Builder()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/v1/charges"))
                .withRequestBody(matchingJsonPath("$.amount"))
                .withRequestBody(matchingJsonPath("$.currency"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "ch_3Nabc123def456",
                                    "object": "charge",
                                    "amount": 10000,
                                    "currency": "usd",
                                    "status": "succeeded"
                                }
                                """)));

        var gateway = new StripePaymentGateway(
                "sk_test_real",
                wireMock.baseUrl() + "/v1/charges",
                new RestTemplate());
        var request = new GatewayPaymentRequest(
                1L,
                new Money(new BigDecimal("100.00"), Currency.getInstance("USD")));

        var result = gateway.process(request);

        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo("ch_3Nabc123def456");
    }

    @Test
    void shouldHandlePaymentFailure() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/v1/charges"))
                .willReturn(aResponse()
                        .withStatus(402)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "error": {
                                        "type": "card_error",
                                        "code": "insufficient_funds",
                                        "message": "Your card has insufficient funds."
                                    }
                                }
                                """)));

        var gateway = new StripePaymentGateway(
                "sk_test_real",
                wireMock.baseUrl() + "/v1/charges",
                new RestTemplate());
        var request = new GatewayPaymentRequest(
                1L,
                new Money(new BigDecimal("100.00"), Currency.getInstance("USD")));

        var result = gateway.process(request);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("insufficient_funds");
    }
}
