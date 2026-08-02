package com.qualitrace.backend.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableRedisHttpSession
@NullMarked
public class RedisSessionConfig {
    private static final String BEARER_PREFIX = "Bearer ";

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new HttpSessionIdResolver() {

            @Override
            public List<String> resolveSessionIds(HttpServletRequest request) {
                String header = request.getHeader("Authorization");
                if (header != null && header.startsWith(BEARER_PREFIX)) {
                    return Collections.singletonList(header.substring(BEARER_PREFIX.length()));
                }
                return Collections.emptyList();
            }

            @Override
            public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {}

            @Override
            public void expireSession(HttpServletRequest request, HttpServletResponse response) {}
        };
    }
}