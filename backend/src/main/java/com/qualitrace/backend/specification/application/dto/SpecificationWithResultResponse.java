package com.qualitrace.backend.specification.application.dto;

import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultMinimalResponse;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "specifications", itemRelation = "specification")
public record SpecificationWithResultResponse(
        Long id,
        String name,
        String method,
        String unit,
        Double min,
        Double max,
        AnalysisResultMinimalResponse results
) {
}
