package com.qualitrace.backend.component.domain.exception;

import com.qualitrace.backend.shared.domain.exception.DomainNotFoundException;

public class ComponentNotFoundException extends DomainNotFoundException {
    public ComponentNotFoundException(Long id) {
        super("Component not found: " + id);
    }
}