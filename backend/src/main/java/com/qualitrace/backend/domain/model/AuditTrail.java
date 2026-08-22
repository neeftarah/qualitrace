package com.qualitrace.backend.domain.model;

import java.time.Instant;

public record AuditTrail(
        Long id,
        User author,
        String event,
        String entity_type,
        String entity_id,
        Instant timestamp,
        String previous_data,
        String changed_data
) {
    public static AuditTrail createNew(
            User author,
            String event,
            String entity_type,
            String entity_id,
            Instant timestamp,
            String previous_data,
            String changed_data
    ) {
        return new AuditTrail(
                null,
                author,
                event,
                entity_type,
                entity_id,
                timestamp,
                previous_data,
                changed_data
        );
    }
}
