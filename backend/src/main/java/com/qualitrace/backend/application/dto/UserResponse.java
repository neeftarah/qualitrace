package com.qualitrace.backend.application.dto;

import com.qualitrace.backend.domain.type.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse (
    UUID id,
    String login,
    String email,
    String firstname,
    String surname,
    UserStatus status,
    Instant createdAt,
    Instant updatedAt
) {}
