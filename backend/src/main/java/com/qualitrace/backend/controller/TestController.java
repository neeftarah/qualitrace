package com.qualitrace.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/v1/test")
    public Map<String, String> test() {
        return Map.of("status", "UP", "message", "QualiTrace API is running on Java 25");
    }
}