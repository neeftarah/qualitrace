package com.qualitrace.backend.analysisresult.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AnalysisResultUpdateRequest(
        @Schema(description = "Valeur du résultat obtenu", example = "7.14")
        @NotNull
        Double value
) {
}