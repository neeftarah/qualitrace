package com.qualitrace.backend.deviation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviationUpdateRequest(
        @Schema(description = "Compte rendu de la déviation", example = "Lorem ipsum dolor sit amet, consectetur ...")
        String comment
) {
}