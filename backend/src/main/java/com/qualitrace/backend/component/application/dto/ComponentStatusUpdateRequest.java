package com.qualitrace.backend.component.application.dto;

import com.qualitrace.backend.component.domain.type.ComponentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ComponentStatusUpdateRequest(
        @Schema(description = "Statut du composant", example = "ACTIVE")
        @NotNull
        ComponentStatus status
) {
}