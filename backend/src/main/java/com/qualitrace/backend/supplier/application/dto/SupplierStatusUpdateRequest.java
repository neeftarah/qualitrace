package com.qualitrace.backend.supplier.application.dto;

import com.qualitrace.backend.supplier.domain.type.SupplierStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record SupplierStatusUpdateRequest(
        @Schema(description = "Statut du fournisseur", example = "ACTIVE")
        @NotNull
        SupplierStatus status
) {
}