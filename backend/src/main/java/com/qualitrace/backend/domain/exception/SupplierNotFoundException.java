package com.qualitrace.backend.domain.exception;

public class SupplierNotFoundException extends DomainNotFoundException {
    public SupplierNotFoundException(Long id) {
        super("Supplier not found: " + id);
    }
}