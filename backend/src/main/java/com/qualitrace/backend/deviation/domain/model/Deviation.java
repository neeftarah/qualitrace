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

    public Deviation update(String comment) {
        return new Deviation(
                this.id,
                this.batchId,
                this.code,
                this.status,
                comment
        );
    }

    public Deviation open() {
        if (this.status == DeviationStatus.OPENED) {
            throw new IllegalStateException("La déviation est déjà ouverte");
        }

        return withStatus(DeviationStatus.OPENED);
    }

    public Deviation close() {
        if (this.status == DeviationStatus.CLOSED) {
            throw new IllegalStateException("La déviation est déjà fermée");
        }

        return withStatus(DeviationStatus.CLOSED);
    }

    private Deviation withStatus(DeviationStatus newStatus) {
        return new Deviation(this.id, this.batchId, this.code, newStatus, this.comment);
    }
}
