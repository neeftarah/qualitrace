package com.qualitrace.backend.supplier.domain.exception;

import com.qualitrace.backend.shared.domain.exception.DomainNotFoundException;

public class SupplierNotFoundException extends DomainNotFoundException {
    public SupplierNotFoundException(Long id) {
        super("Supplier not found: " + id);
    }
}