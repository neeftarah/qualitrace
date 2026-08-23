package com.qualitrace.backend.component.domain.model;

import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;

public record ComponentFilter(
        ComponentType type,
        String reference,
        String name,
        ComponentStatus status,
        Long supplierId
) {
    public static ComponentFilter empty() {
        return new ComponentFilter(null, null, null, null, null);
    }
}
