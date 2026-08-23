package com.qualitrace.backend.supplier.domain.model;

import com.qualitrace.backend.supplier.domain.type.SupplierStatus;

public record SupplierFilter(
        String code,
        String name,
        SupplierStatus status
) {
    public static SupplierFilter empty() {
        return new SupplierFilter(null, null, null);
    }
}