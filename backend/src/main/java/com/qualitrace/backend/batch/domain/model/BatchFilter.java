package com.qualitrace.backend.batch.domain.model;

import com.qualitrace.backend.batch.domain.type.BatchStatus;

import java.time.LocalDate;
import java.util.UUID;

public record BatchFilter(
        String internalBatchNumber,
        Long supplierId,
        String supplierBatchNumber,
        LocalDate expiryFromDate,
        LocalDate expiryToDate,
        LocalDate receptionFromDate,
        LocalDate receptionToDate,
        BatchStatus status,
        UUID validatedBy,
        LocalDate validationFromDate,
        LocalDate validationToDate
) {
}
