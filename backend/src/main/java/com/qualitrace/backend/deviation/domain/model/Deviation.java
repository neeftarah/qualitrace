package com.qualitrace.backend.deviation.domain.model;

import com.qualitrace.backend.deviation.domain.type.DeviationStatus;

public record Deviation(
        Long id,
        Long batchId,
        String code,
        DeviationStatus status,
        String comment
) {
    public static Deviation createNew(
            Long batchId,
            String code,
            DeviationStatus status,
            String comment
    ) {
        if (batchId == null) {
            throw new IllegalArgumentException("Batch ID cannot be null");
        }
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }

        return new Deviation(
                null, // Placeholder ID, will be replaced by the repository
                batchId,
                code,
                status != null ? status : DeviationStatus.OPENED,
                comment != null ? comment : ""
        );
    }
}
