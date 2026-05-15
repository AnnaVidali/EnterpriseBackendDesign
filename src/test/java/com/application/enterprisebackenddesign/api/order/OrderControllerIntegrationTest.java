package com.application.enterprisebackenddesign.api.order;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Sql(statements = {
        "DELETE FROM order_lines",
        "DELETE FROM orders",
        "DELETE FROM customers",
        "DELETE FROM products"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderControllerIntegrationTest {

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

    private String createOrder(Long customerId, Long productId) throws Exception {
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

        return mockMvc.perform(post("/api/orders")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void shouldCreateOrder() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-001");

        String orderResponse = createOrder(customerId, productId);

        mockMvc.perform(get("/api/orders/{id}", objectMapper.readTree(orderResponse).get("id").asLong())
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.lines", hasSize(1)));
    }

    @Test
    void shouldConfirmOrder() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-002");
        String orderResponse = createOrder(customerId, productId);
        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/confirm", orderId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldCancelOrder() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-003");
        String orderResponse = createOrder(customerId, productId);
        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldAddOrderLine() throws Exception {
        Long customerId = createCustomer();
        Long productId1 = createProduct("SKU-004");
        Long productId2 = createProduct("SKU-005");
        String orderResponse = createOrder(customerId, productId1);
        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        ObjectNode lineRequest = objectMapper.createObjectNode();
        lineRequest.put("productId", productId2);
        lineRequest.put("quantity", 3);
        lineRequest.put("price", 15.00);
        lineRequest.put("currency", "USD");

        mockMvc.perform(post("/api/orders/{id}/lines", orderId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines", hasSize(2)));
    }

    @Test
    void shouldUpdateOrderLine() throws Exception {
        Long customerId = createCustomer();
        Long productId = createProduct("SKU-006");
        String orderResponse = createOrder(customerId, productId);
        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();
        Long lineId = objectMapper.readTree(orderResponse).get("lines").get(0).get("id").asLong();

        ObjectNode updateRequest = objectMapper.createObjectNode();
        updateRequest.put("productId", productId);
        updateRequest.put("quantity", 5);
        updateRequest.put("price", 10.00);
        updateRequest.put("currency", "USD");

        mockMvc.perform(put("/api/orders/{id}/lines/{lineId}", orderId, lineId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].quantity").value(5));
    }

    @Test
    void shouldRemoveOrderLine() throws Exception {
        Long customerId = createCustomer();
        Long productId1 = createProduct("SKU-007");
        Long productId2 = createProduct("SKU-008");
        String orderResponse = createOrder(customerId, productId1);
        Long orderId = objectMapper.readTree(orderResponse).get("id").asLong();

        ObjectNode lineRequest = objectMapper.createObjectNode();
        lineRequest.put("productId", productId2);
        lineRequest.put("quantity", 1);
        lineRequest.put("price", 15.00);
        lineRequest.put("currency", "USD");

        String addResponse = mockMvc.perform(post("/api/orders/{id}/lines", orderId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long lineId2 = objectMapper.readTree(addResponse).get("lines").get(1).get("id").asLong();

        mockMvc.perform(delete("/api/orders/{id}/lines/{lineId}", orderId, lineId2)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines", hasSize(1)));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/9999")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }
}
