package com.qualitrace.backend.batch.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record BatchCreateRequest(
        @Schema(description = "ID du composant", example = "1")
        @NotNull(message = "L'ID du composant est obligatoire")
        @Min(value = 1, message = "L'ID du composant doit être supérieur ou égal à 1")
        Long componentId,

        @Schema(description = "Référence fournisseur du lot", example = "SUP-LOT-999")
        @NotBlank
        @Size(min = 1, max = 255)
        String supplierBatchNumber,

        @Schema(description = "Date de péremption")
        @NotNull(message = "La date de péremption est obligatoire")
        Instant expiryDate
) {
}
