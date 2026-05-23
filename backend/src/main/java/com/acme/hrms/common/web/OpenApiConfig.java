package com.acme.hrms.common.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * springdoc bootstrap. Adds a single global security scheme so that
 * "Authorize" in Swagger UI accepts a Keycloak bearer token and applies it
 * to every operation by default.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearer-jwt";

    @Bean
    public OpenAPI hrmsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HRMS API")
                        .version("v1")
                        .description("Internal HR / Resource Management / Billing Analytics platform"))
                .components(new Components().addSecuritySchemes(
                        SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Keycloak-issued access token. Get one via the SPA login flow.")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
    }
}
