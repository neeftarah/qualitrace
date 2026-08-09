package com.qualitrace.backend.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierUpdateRequest(
        @Schema(description = "Nom du fournisseur", example = "Acme Corporation")
        @NotBlank
        @Size(min = 1, max = 100)
        String name,

        @Schema(description = "Adresse", example = "123 Main Street")
        @NotBlank
        @Size(min = 1, max = 2000)
        String address
) {
}