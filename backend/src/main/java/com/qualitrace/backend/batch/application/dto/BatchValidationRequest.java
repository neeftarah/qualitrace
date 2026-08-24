package com.qualitrace.backend.batch.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BatchValidationRequest(
        @Schema(description = "Validation/refus du lot", example = "true|false")
        @NotBlank
        boolean accept
) {
}
