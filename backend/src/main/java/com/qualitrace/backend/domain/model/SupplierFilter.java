package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.domain.type.SupplierStatus;

public record SupplierFilter(
        String code,
        String name,
        SupplierStatus status
) {
    public static SupplierFilter empty() {
        return new SupplierFilter(null, null, null);
    }
}