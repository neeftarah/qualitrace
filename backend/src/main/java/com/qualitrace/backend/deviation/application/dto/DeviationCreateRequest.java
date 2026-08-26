package com.qualitrace.backend.deviation.application.dto;

import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviationCreateRequest(
        @Schema(description = "Code de la déviation", example = "DEV-202608-254")
        @NotBlank
        @Size(min = 1, max = 255)
        String code,

        @Schema(description = "Statut de la déviation (optionnel ; OPENED par défaut).", example = "OPENED|CLOSED")
        DeviationStatus status,

        @Schema(description = "Compte rendu de la déviation", example = "Lorem ipsum dolor sit amet, consectetur ...")
        String comment
) {
}