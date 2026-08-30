package com.qualitrace.backend.batch.application.dto;

import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.component.application.dto.ComponentResponse;
import com.qualitrace.backend.deviation.application.dto.DeviationResponse;
import com.qualitrace.backend.specification.application.dto.SpecificationWithResultResponse;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;
import java.util.List;

@Relation(collectionRelation = "batches", itemRelation = "batch")
public record BatchDetailResponse(
        Long id,
        String internalBatchNumber,
        String supplierBatchNumber,
        Instant expiryDate,
        Instant receptionDate,
        BatchStatus status,
        Instant validatedAt,
        String validatedBy,
        ComponentResponse component,
        List<SpecificationWithResultResponse> specifications,
        List<DeviationResponse> deviations
) {
}
