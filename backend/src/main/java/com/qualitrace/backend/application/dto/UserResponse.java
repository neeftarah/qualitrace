package com.qualitrace.backend.application.dto;

import com.qualitrace.backend.domain.type.UserRole;
import com.qualitrace.backend.domain.type.UserStatus;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Relation(collectionRelation = "users", itemRelation = "user")
public record UserResponse (
    UUID id,
    String login,
    String email,
    String firstname,
    String surname,
    UserStatus status,
    Set<UserRole> roles,
    Instant createdAt,
    Instant updatedAt
) {}
