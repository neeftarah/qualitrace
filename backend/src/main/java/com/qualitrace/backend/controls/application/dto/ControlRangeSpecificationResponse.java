package com.qualitrace.backend.controls.application.dto;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "controlRangeSpecifications", itemRelation = "controlRangeSpecification")
public record ControlRangeSpecificationResponse(
        Long id,
        String name,
        String method,
        String unit,
        Double min,
        Double max,
        Long componentId
) {
}
