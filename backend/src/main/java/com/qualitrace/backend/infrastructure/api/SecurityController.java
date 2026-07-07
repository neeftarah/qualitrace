package com.qualitrace.backend.infrastructure.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SecurityController {

    @PostMapping("/api/v1/login")
    public Map<String, String> login() {
        return Map.of("controller", "SecurityController", "method", "login");
    }

    @PostMapping("/api/v1/logout")
    public Map<String, String> logout() {
        return Map.of("controller", "SecurityController", "method", "logout");
    }
}