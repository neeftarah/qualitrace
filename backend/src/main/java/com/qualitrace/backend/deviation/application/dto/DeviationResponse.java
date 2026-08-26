package com.qualitrace.backend.deviation.application.dto;

import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "deviations", itemRelation = "deviation")
public record DeviationResponse(
        Long id,
        String code,
        DeviationStatus status,
        String comment,
        Long batchId
) {
}
