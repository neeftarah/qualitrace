package com.qualitrace.backend.user.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank
    String login,

    @NotBlank
    String password
) {}
