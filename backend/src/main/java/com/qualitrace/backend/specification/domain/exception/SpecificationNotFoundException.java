package com.qualitrace.backend.specification.domain.exception;

import com.qualitrace.backend.shared.domain.exception.DomainNotFoundException;

public class SpecificationNotFoundException extends DomainNotFoundException {
    public SpecificationNotFoundException(Long id) {
        super("Control range specification not found: " + id);
    }
}