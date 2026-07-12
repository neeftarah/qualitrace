package com.qualitrace.backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI qualitraceOpenAPI() {
        return new OpenAPI().info(new Info()
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
                .license(new License().name("Projet académique CNAM - GLG204")));
    }
}