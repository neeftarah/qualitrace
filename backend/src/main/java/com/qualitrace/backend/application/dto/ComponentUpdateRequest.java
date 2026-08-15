package com.qualitrace.backend.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComponentUpdateRequest(
        @Schema(description = "Nom du composant", example = "Matériau de haute qualité")
        @NotBlank
        @Size(min = 1, max = 100)
        String name
) {
}