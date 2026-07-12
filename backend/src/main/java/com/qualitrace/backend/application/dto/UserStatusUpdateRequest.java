package com.qualitrace.backend.application.dto;

import com.qualitrace.backend.domain.type.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @Schema(description = "Statut de l'utilisateur", example = "ACTIVE")
        @NotNull
        UserStatus status
) {}