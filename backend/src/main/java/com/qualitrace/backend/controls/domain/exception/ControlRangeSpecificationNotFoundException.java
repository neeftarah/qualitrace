package com.qualitrace.backend.controls.domain.exception;

import com.qualitrace.backend.shared.domain.exception.DomainNotFoundException;

public class ControlRangeSpecificationNotFoundException extends DomainNotFoundException {
    public ControlRangeSpecificationNotFoundException(Long id) {
        super("Control range specification not found: " + id);
    }
}