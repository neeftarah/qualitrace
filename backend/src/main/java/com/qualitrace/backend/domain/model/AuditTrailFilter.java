package com.qualitrace.backend.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public record AuditTrailFilter(
        UUID author_id,
        String event,
        String entity_type,
        String entity_id,
        String content,
        LocalDate fromDate,
        LocalDate toDate
) {
    public static AuditTrailFilter empty() {
        return new AuditTrailFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}