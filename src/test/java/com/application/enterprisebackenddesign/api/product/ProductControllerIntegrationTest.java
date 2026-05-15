package com.application.enterprisebackenddesign.api.product;

import com.application.enterprisebackenddesign.TestcontainersConfiguration;
import com.application.enterprisebackenddesign.infrastructure.security.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@Sql(statements = "DELETE FROM products", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductControllerIntegrationTest {

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

    @Test
    void shouldCreateProduct() throws Exception {
        var request = new ProductRequest("Widget", BigDecimal.TEN, "USD", "WID-001");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.price").value(10.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.sku").value("WID-001"));
    }

    @Test
    void shouldGetProductById() throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Widget", BigDecimal.TEN, "USD", "WID-001"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", id)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Widget"));
    }

    @Test
    void shouldListAllProductsWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Widget", BigDecimal.TEN, "USD", "WID-001"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Widget", BigDecimal.TEN, "USD", "WID-001"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        var updateRequest = new ProductRequest("Gadget", new BigDecimal("20.00"), "USD", "GDG-001");

        mockMvc.perform(put("/api/products/{id}", id)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gadget"))
                .andExpect(jsonPath("$.sku").value("GDG-001"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Widget", BigDecimal.TEN, "USD", "WID-001"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/products/{id}", id)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        mockMvc.perform(get("/api/products/9999")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenCreatingProductWithInvalidBody() throws Exception {
        var request = new ProductRequest("", null, "", "");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
        var request = new ProductRequest("Gadget", new BigDecimal("20.00"), "USD", "GDG-001");

        mockMvc.perform(put("/api/products/9999")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
        mockMvc.perform(delete("/api/products/9999")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }
}
