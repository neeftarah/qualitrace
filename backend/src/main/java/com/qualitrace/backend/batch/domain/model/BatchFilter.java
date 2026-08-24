package com.qualitrace.backend.batch.domain.model;

import com.qualitrace.backend.batch.domain.type.BatchStatus;

import java.time.LocalDate;
import java.util.UUID;

public record BatchFilter(
        String internalReferenceNumber,
        Long supplierId,
        String supplierReferenceNumber,
        LocalDate expiryFromDate,
        LocalDate expiryToDate,
        LocalDate receptionFromDate,
        LocalDate receptionToDate,
        BatchStatus status,
        UUID validatedBy,
        LocalDate validationFromDate,
        LocalDate validationToDate
) {
    public static BatchFilter empty() {
        return new BatchFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
