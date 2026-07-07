package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.domain.type.UserStatus;

import java.time.Instant;
import java.util.UUID;

public class User {
    private final UUID id;
    private final String login;
    private final String password;
    private final String email;
    private final String firstname;
    private final String surname;
    private final UserStatus status;
    private final Long version;
    private final Instant createdAt;
    private final Instant updatedAt;

    public User(UUID id, String login, String password, String email, String firstname,
                String surname, UserStatus status, Long version, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.email = email;
        this.firstname = firstname;
        this.surname = surname;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User createNew(String login, String password, String email, String firstname, String surname) {
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
            null
        );
    }

    public UUID getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getFirstname() { return firstname; }
    public String getSurname() { return surname; }
    public UserStatus getStatus() { return status; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
