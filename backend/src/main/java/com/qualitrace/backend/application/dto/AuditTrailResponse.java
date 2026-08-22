package com.qualitrace.backend.application.dto;

import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Relation(collectionRelation = "audit_trails", itemRelation = "audit_trail")
public record AuditTrailResponse(
        Long id,
        UserResponse author,
        String event,
        String entity_type,
        String entity_id,
        Instant timestamp,
        String previous_data,
        String changed_data
) {
}
