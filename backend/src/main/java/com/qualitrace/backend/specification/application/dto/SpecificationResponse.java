package com.qualitrace.backend.specification.application.dto;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "specifications", itemRelation = "specification")
public record SpecificationResponse(
        Long id,
        String name,
        String method,
        String unit,
        Double min,
        Double max,
        Long componentId
) {
}
