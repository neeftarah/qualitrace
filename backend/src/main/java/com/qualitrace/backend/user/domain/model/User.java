package com.qualitrace.backend.user.domain.model;

import com.qualitrace.backend.user.domain.type.UserRole;
import com.qualitrace.backend.user.domain.type.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record User(UUID id, String login, String password, String email, String firstname, String surname,
                   UserStatus status, Long version, Instant createdAt, Instant updatedAt, Set<UserRole> roles) {

    public static User createNew(String login, String password, String email, String firstname, String surname, Set<UserRole> roles) {
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }

        return new User(
                UUID.randomUUID(),
                login,
                password,
                email,
                firstname,
                surname,
                UserStatus.ACTIVE,
                0L,
                Instant.now(),
                null,
                roles
        );
    }

    public User update(String firstname, String surname, Set<UserRole> roles) {
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }

        return new User(
                this.id,
                this.login,
                this.password,
                this.email,
                firstname,
                surname,
                this.status,
                this.version,
                this.createdAt,
                Instant.now(),
                roles
        );
    }

    public User lock() {
        if (this.status != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Seul un utilisateur actif peut être verrouillé (statut actuel : %s)".formatted(this.status));
        }
        return withStatus(UserStatus.LOCKED);
    }

    public User unlock() {
        if (this.status != UserStatus.LOCKED) {
            throw new IllegalStateException(
                    "Seul un utilisateur verrouillé peut être déverrouillé (statut actuel : %s)".formatted(this.status));
        }
        return withStatus(UserStatus.ACTIVE);
    }

    public User archive() {
        if (this.status == UserStatus.ARCHIVED) {
            throw new IllegalStateException("L'utilisateur est déjà archivé");
        }
        return withStatus(UserStatus.ARCHIVED);
    }

    public User reactivate() {
        if (this.status != UserStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Seul un utilisateur archivé peut être réactivé (statut actuel : %s)".formatted(this.status));
        }
        return withStatus(UserStatus.ACTIVE);
    }

    private User withStatus(UserStatus newStatus) {
        return new User(this.id, this.login, this.password, this.email, this.firstname, this.surname,
                newStatus, this.version, this.createdAt, Instant.now(), this.roles);
    }
}
