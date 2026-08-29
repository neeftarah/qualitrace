package com.qualitrace.backend.analysisresult.application.dto;

import com.qualitrace.backend.user.application.dto.UserResponse;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Relation(collectionRelation = "analysisResults", itemRelation = "analysisResult")
public record AnalysisResultResponse(
        Long id,
        Long batchId,
        Long specificationId,
        Double value,
        Instant createdAt,
        UserResponse createdBy
) {
}
