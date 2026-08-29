package com.qualitrace.backend.analysisresult.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record AnalysisResultCreateRequest(
        @Schema(description = "Identifiant du contrôle concerné", example = "254")
        @NotNull
        @Min(1)
        Long specificationId,

        @Schema(description = "Valeur du résultat obtenu", example = "7.14")
        @NotNull
        Double value
) {
}
