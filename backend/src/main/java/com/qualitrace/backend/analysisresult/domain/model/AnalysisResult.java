package com.qualitrace.backend.analysisresult.domain.model;

import com.qualitrace.backend.user.domain.model.User;

import java.time.Instant;

public record AnalysisResult(
        Long id,
        Long batchId,
        Long specificationId,
        Double value,
        Instant createdAt,
        User createdBy
) {
    public static AnalysisResult createNew(
            Long batchId,
            Long specificationId,
            Double value,
            User createdBy
    ) {
        if (batchId == null) {
            throw new IllegalArgumentException("Batch ID cannot be null");
        }
        if (specificationId == null) {
            throw new IllegalArgumentException("Specification ID cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("Creator user cannot be null");
        }

        return new AnalysisResult(
                null, // Placeholder ID, will be replaced by the repository
                batchId,
                specificationId,
                value,
                Instant.now(),
                createdBy
        );
    }

    public AnalysisResult update(Double value) {
        return new AnalysisResult(
                this.id,
                this.batchId,
                this.specificationId,
                value,
                this.createdAt,
                this.createdBy
        );
    }
}
