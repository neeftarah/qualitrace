package com.qualitrace.backend.specification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecificationUpdateRequest(
        @Schema(description = "Nom de la méthode de contrôle utilisée", example = "PctNaOH-7832")
        @NotBlank
        @Size(min = 1, max = 255)
        String method,

        @Schema(description = "Valeur minimale valide", example = "25")
        Double min,

        @Schema(description = "Valeur maximale valide", example = "35")
        Double max
) {
}