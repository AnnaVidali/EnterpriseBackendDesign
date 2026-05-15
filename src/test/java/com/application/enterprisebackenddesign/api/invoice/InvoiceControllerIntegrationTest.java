package com.application.enterprisebackenddesign.api.invoice;

import com.application.enterprisebackenddesign.TestcontainersConfiguration;
import com.application.enterprisebackenddesign.api.customer.CustomerRequest;
import com.application.enterprisebackenddesign.api.product.ProductRequest;
import com.application.enterprisebackenddesign.infrastructure.security.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Sql(statements = {
        "DELETE FROM invoices",
        "DELETE FROM order_lines",
        "DELETE FROM orders",
        "DELETE FROM customers",
        "DELETE FROM products"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String authHeader;

    @BeforeEach
    void setUp() {
        authHeader = "Bearer " + jwtProvider.generateToken("admin");
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

    @Test
    void shouldIssueInvoiceForConfirmedOrder() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-010");
        Long orderId = createAndConfirmOrder(customerId, productId);

        mockMvc.perform(post("/api/invoices/issue/{orderId}", orderId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.amount").isNumber());
    }

    @Test
    void shouldGetInvoiceById() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-011");
        Long orderId = createAndConfirmOrder(customerId, productId);

        String invoiceResponse = mockMvc.perform(post("/api/invoices/issue/{orderId}", orderId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long invoiceId = objectMapper.readTree(invoiceResponse).get("id").asLong();

        mockMvc.perform(get("/api/invoices/{invoiceId}", invoiceId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId))
                .andExpect(jsonPath("$.status").value("ISSUED"));
    }

    @Test
    void shouldListInvoices() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-012");
        Long orderId = createAndConfirmOrder(customerId, productId);

        mockMvc.perform(post("/api/invoices/issue/{orderId}", orderId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/invoices")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldReturn404WhenInvoiceNotFound() throws Exception {
        mockMvc.perform(get("/api/invoices/9999")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenIssuingInvoiceForNonExistentOrder() throws Exception {
        mockMvc.perform(post("/api/invoices/issue/9999")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }
}
