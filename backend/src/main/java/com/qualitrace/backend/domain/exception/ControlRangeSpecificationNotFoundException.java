package com.qualitrace.backend.domain.exception;

public class ControlRangeSpecificationNotFoundException extends DomainNotFoundException {
    public ControlRangeSpecificationNotFoundException(Long id) {
        super("Control range specification not found: " + id);
    }
}