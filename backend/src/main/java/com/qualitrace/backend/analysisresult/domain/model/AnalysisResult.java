package com.qualitrace.backend.analysisresult.domain.model;

import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.user.domain.model.User;

import java.time.Instant;
import java.util.Optional;

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
            User createdBy,
            BatchRepository batchRepository
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
        Optional<Batch> batch = batchRepository.findById(batchId);
        if (batch.isEmpty() || batch.get().status() != BatchStatus.QUARANTINE) {
            throw new IllegalArgumentException("The batch must not have been validated");
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

    public AnalysisResult update(Double value, BatchRepository batchRepository) {
        Optional<Batch> batch = batchRepository.findById(this.batchId);
        if (batch.isEmpty() || batch.get().status() != BatchStatus.QUARANTINE) {
            throw new IllegalArgumentException("The batch must not have been validated");
        }

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
