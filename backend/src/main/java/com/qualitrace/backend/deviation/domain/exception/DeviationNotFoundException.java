package com.qualitrace.backend.deviation.domain.exception;

import com.qualitrace.backend.shared.domain.exception.DomainNotFoundException;

public class DeviationNotFoundException extends DomainNotFoundException {
    public DeviationNotFoundException(Long id) {
        super("Deviation not found: " + id);
    }
}