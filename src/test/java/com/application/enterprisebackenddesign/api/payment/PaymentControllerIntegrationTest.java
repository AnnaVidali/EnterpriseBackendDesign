package com.application.enterprisebackenddesign.api.payment;

import com.application.enterprisebackenddesign.TestcontainersConfiguration;
import com.application.enterprisebackenddesign.api.customer.CustomerRequest;
import com.application.enterprisebackenddesign.api.product.ProductRequest;
import com.application.enterprisebackenddesign.infrastructure.external.GatewayPaymentRequest;
import com.application.enterprisebackenddesign.infrastructure.external.PaymentGateway;
import com.application.enterprisebackenddesign.infrastructure.external.PaymentResult;
import com.application.enterprisebackenddesign.infrastructure.security.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Sql(statements = {
        "DELETE FROM payments",
        "DELETE FROM invoices",
        "DELETE FROM order_lines",
        "DELETE FROM orders",
        "DELETE FROM customers",
        "DELETE FROM products"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PaymentGateway paymentGateway;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String authHeader;

    @TestConfiguration
    static class MockPaymentGatewayConfig {
        @Bean
        @Primary
        PaymentGateway mockPaymentGateway() {
            return org.mockito.Mockito.mock(PaymentGateway.class);
        }
    }

    @BeforeEach
    void setUp() {
        authHeader = "Bearer " + jwtProvider.generateToken("admin");
        when(paymentGateway.process(any(GatewayPaymentRequest.class)))
                .thenReturn(new PaymentResult(true, "txn-test-123", "Payment successful"));
    }

    private Long createCustomer() throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomerRequest("John", "Doe", "john@example.com"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createProduct(String sku) throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Product-" + sku, BigDecimal.TEN, "USD", sku))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createAndConfirmOrder(Long customerId, Long productId) throws Exception {
        ObjectNode line = objectMapper.createObjectNode();
        line.put("productId", productId);
        line.put("quantity", 2);
        line.put("price", 10.00);
        line.put("currency", "USD");

        ArrayNode lines = objectMapper.createArrayNode();
        lines.add(line);

        ObjectNode orderRequest = objectMapper.createObjectNode();
        orderRequest.put("customerId", customerId);
        orderRequest.put("currency", "USD");
        orderRequest.set("lines", lines);

        String orderResponse = mockMvc.perform(post("/api/orders")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/confirm", orderId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        return orderId;
    }

    private Long issueInvoice(Long orderId) throws Exception {
        String invoiceResponse = mockMvc.perform(post("/api/invoices/issue/{orderId}", orderId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(invoiceResponse).get("id").asLong();
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-020");
        Long orderId = createAndConfirmOrder(customerId, productId);
        Long invoiceId = issueInvoice(orderId);

        ObjectNode paymentRequest = objectMapper.createObjectNode();
        paymentRequest.put("invoiceId", invoiceId);
        paymentRequest.put("amount", 20.00);
        paymentRequest.put("currency", "USD");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.invoiceId").value(invoiceId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldGetPaymentById() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-021");
        Long orderId = createAndConfirmOrder(customerId, productId);
        Long invoiceId = issueInvoice(orderId);

        ObjectNode paymentRequest = objectMapper.createObjectNode();
        paymentRequest.put("invoiceId", invoiceId);
        paymentRequest.put("amount", 20.00);
        paymentRequest.put("currency", "USD");

        String paymentResponse = mockMvc.perform(post("/api/payments")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long paymentId = objectMapper.readTree(paymentResponse).get("id").asLong();

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldListPayments() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-022");
        Long orderId = createAndConfirmOrder(customerId, productId);
        Long invoiceId = issueInvoice(orderId);

        ObjectNode paymentRequest = objectMapper.createObjectNode();
        paymentRequest.put("invoiceId", invoiceId);
        paymentRequest.put("amount", 20.00);
        paymentRequest.put("currency", "USD");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/payments")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldReturn404WhenPaymentNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/9999")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenProcessingPaymentForNonExistentInvoice() throws Exception {
        ObjectNode paymentRequest = objectMapper.createObjectNode();
        paymentRequest.put("invoiceId", 9999);
        paymentRequest.put("amount", 20.00);
        paymentRequest.put("currency", "USD");

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound());
    }
}
