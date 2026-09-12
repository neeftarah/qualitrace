package com.qualitrace.backend.user.domain.model;

import com.qualitrace.backend.user.domain.type.UserRole;
import com.qualitrace.backend.user.domain.type.UserStatus;

import java.time.LocalDate;

public record UserFilter(
        String login,
        String email,
        String firstname,
        String surname,
        UserStatus status,
        UserRole role,
        LocalDate fromCreationDate,
        LocalDate toCreationDate,
        LocalDate fromUpdateDate,
        LocalDate toUpdateDate
) {
}