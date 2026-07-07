package com.qualitrace.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
    @NotBlank String login,
    @NotBlank String password,
    @NotBlank @Email String email,
    @NotBlank String firstname,
    @NotBlank String surname
) {}