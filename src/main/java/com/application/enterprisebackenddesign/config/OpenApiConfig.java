package com.application.enterprisebackenddesign.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI documentation configuration.
 *
 * Infrastructure concern: Generates the Swagger/OpenAPI specification from
 * code annotations (@Operation, @ApiResponse, etc.). The spec is available
 * at /v3/api-docs and the interactive UI at /swagger-ui.html.
 *
 * Architectural note: The API spec is generated from code, not hand-maintained.
 * This ensures the documentation stays in sync with the implementation.
 * The JWT security scheme is defined here so all endpoints documented in
 * Swagger show the "Authorize" button automatically.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise Backend Design API")
                        .version("1.0.0")
                        .description("RESTful API for the Enterprise Order and Billing Service. " +
                                "Manages orders, invoices, payments, customers, and products " +
                                "with full domain event publishing.")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@enterprise-backend.com")
                                .url("https://github.com/anomalyco/EnterPriseBackendDesign"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer JWT"))
                .components(new Components()
                        .addSecuritySchemes("Bearer JWT", new SecurityScheme()
                                .name("Bearer JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide a JWT token obtained from POST /api/auth/login")));
    }
}
