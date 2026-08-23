package com.qualitrace.backend.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI qualitraceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                    .title("Qualitrace API")
                    .description("""
                            API de gestion et de traçabilité pharmaceutique (GxP).<br />
                            Gère les utilisateurs, fournisseurs, composants et lots
                            dans le respect des exigences de traçabilité (21 CFR Part 11 / Annexe 11).
                            """)
                    .version("v1")
                    .contact(new Contact()
                            .name("Jérémy Moreau")
                            .email("jmoreau.dev@gmail.com"))
                    .license(new License().name("Projet académique CNAM - GLG204")))
                .components(new Components()
                    .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("opaque") // pas un JWT, juste un ID de session
                            .description("Token obtenu via POST /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}