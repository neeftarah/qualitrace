package com.qualitrace.backend.application.dto;

import com.qualitrace.backend.domain.model.Supplier;
import com.qualitrace.backend.domain.type.ComponentStatus;
import com.qualitrace.backend.domain.type.ComponentType;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Relation(collectionRelation = "components", itemRelation = "component")
public record ComponentResponse(
        Long id,
        ComponentType type,
        String reference,
        String name,
        Instant availableFrom,
        ComponentStatus status,
        Supplier supplier
) {
}
