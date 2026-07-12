package com.qualitrace.backend.domain.exception;

import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;

public class UserNotFoundException extends DomainNotFoundException {
    public UserNotFoundException(UUID id) {
        super("User not found: " + id);
    }
}