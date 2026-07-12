package com.qualitrace.backend.domain.exception;

public class DomainNotFoundException extends RuntimeException {
    protected DomainNotFoundException(String message) {
        super(message);
    }
}
