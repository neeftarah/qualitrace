package com.qualitrace.backend.domain.exception;

public class ComponentNotFoundException extends DomainNotFoundException {
    public ComponentNotFoundException(Long id) {
        super("Component not found: " + id);
    }
}