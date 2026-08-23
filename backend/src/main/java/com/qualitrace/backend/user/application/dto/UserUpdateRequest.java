package com.qualitrace.backend.user.application.dto;

import com.qualitrace.backend.user.domain.type.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UserUpdateRequest(
        @Schema(description = "Prénom", example = "John")
        String firstname,

        @Schema(description = "Nom de famille", example = "Doe")
        String surname,

        @Schema(description = "Rôles attribués, au moins un requis", example = "AQ")
        @NotEmpty
        Set<UserRole> roles
) {}