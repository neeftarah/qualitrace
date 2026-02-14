package com.qualitrace.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactive le CSRF (inutile pour une API REST Stateless avec JWT/Token)
            .csrf(AbstractHttpConfigurer::disable)

            // Configure la gestion de session en "Stateless" (pas de JSESSIONID)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Autorise tout le monde à accéder aux endpoints pour le moment
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll()
            )

            // Désactive explicitement le formulaire de login et le HTTP Basic par défaut
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}