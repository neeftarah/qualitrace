package com.qualitrace.backend.batch.domain.exception;

import com.qualitrace.backend.shared.domain.exception.DomainNotFoundException;

public class BatchNotFoundException extends DomainNotFoundException {
    public BatchNotFoundException(Long id) {
        super("Component not found: " + id);
    }
}