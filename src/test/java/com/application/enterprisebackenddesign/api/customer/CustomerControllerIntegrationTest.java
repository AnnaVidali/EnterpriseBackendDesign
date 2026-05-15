package com.application.enterprisebackenddesign.api.customer;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Sql(statements = "DELETE FROM customers", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CustomerControllerIntegrationTest {

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
    void shouldCreateCustomer() throws Exception {
        var request = new CustomerRequest("John", "Doe", "john@example.com");

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomerRequest("John", "Doe", "john@example.com"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/customers/{id}", id)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void shouldListAllCustomers() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomerRequest("John", "Doe", "john@example.com"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomerRequest("Jane", "Smith", "jane@example.com"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/customers")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldUpdateCustomer() throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomerRequest("John", "Doe", "john@example.com"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        var updateRequest = new CustomerRequest("Jane", "Smith", "jane@example.com");

        mockMvc.perform(put("/api/customers/{id}", id)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        String response = mockMvc.perform(post("/api/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomerRequest("John", "Doe", "john@example.com"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/customers/{id}", id)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/9999")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenCreatingCustomerWithInvalidBody() throws Exception {
        var request = new CustomerRequest("", "", "invalid-email");

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors", hasSize(3)));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentCustomer() throws Exception {
        var request = new CustomerRequest("Jane", "Smith", "jane@example.com");

        mockMvc.perform(put("/api/customers/9999")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/9999")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }
}
