package com.qualitrace.backend.user.domain.model;

import com.qualitrace.backend.user.domain.type.UserRole;
import com.qualitrace.backend.user.domain.type.UserStatus;

public record UserFilter(
        String login,      // recherche partielle
        String email,      // recherche partielle
        String firstname,  // recherche partielle
        String surname,    // recherche partielle
        UserStatus status, // exact
        UserRole role       // exact — présence de ce rôle dans le tableau
) {
    public static UserFilter empty() {
        return new UserFilter(null, null, null, null, null, null);
    }
}