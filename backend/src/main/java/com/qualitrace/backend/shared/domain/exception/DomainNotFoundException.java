package com.qualitrace.backend.shared.domain.exception;

public class DomainNotFoundException extends RuntimeException {
    protected DomainNotFoundException(String message) {
        super(message);
    }
}
