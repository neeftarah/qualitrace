package com.qualitrace.backend.user.domain.exception;

import com.qualitrace.backend.shared.domain.exception.DomainNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends DomainNotFoundException {
    public UserNotFoundException(UUID id) {
        super("User not found: " + id);
    }
}