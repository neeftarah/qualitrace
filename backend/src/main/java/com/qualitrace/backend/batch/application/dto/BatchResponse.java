package com.qualitrace.backend.batch.application.dto;

import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.component.domain.model.Component;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Relation(collectionRelation = "batches", itemRelation = "batch")
public record BatchResponse(
        Long id,
        Component component,
        String internalReferenceNumber,
        String supplierReferenceNumber,
        Instant expiryDate,
        Instant receptionDate,
        BatchStatus status
) {
}
