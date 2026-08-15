package com.qualitrace.backend.application.dto;

import com.qualitrace.backend.domain.type.ComponentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComponentCreateRequest(
        @Schema(description = "Type du composant", example = "RAW_MATERIAL")
        ComponentType type,

        @Schema(description = "Référence unique du composant", example = "CMP-001")
        @NotBlank
        @Size(min = 1, max = 100)
        String reference,

        @Schema(description = "Nom du composant", example = "Matériau de haute qualité")
        @NotBlank
        @Size(min = 1, max = 100)
        String name,

        @Schema(description = "ID du fournisseur", example = "1")
        @Min(value = 1, message = "L'ID du fournisseur doit être supérieur ou égal à 1")
        Long supplierId
) {
}