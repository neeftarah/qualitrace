package com.qualitrace.backend.user.infrastructure.persistence.entity;

import com.qualitrace.backend.user.domain.type.UserRole;
import com.qualitrace.backend.user.domain.type.UserStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class UserEntity implements Persistable<UUID> {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "firstname", nullable = false)
    private String firstname;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private UserStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "roles", columnDefinition = "user_role[]")
    @ColumnTransformer(write = "?::user_role[]")
    private String[] roles = new String[0];

    @Transient
    private boolean isNew;

    // Constructeur standard requis par JPA
    protected UserEntity() {}

    // Constructeur métier (Exemple)
    public UserEntity(UUID id, String login, String password, String email, String firstname, String surname,
                      UserStatus status, Long version, Instant createdAt, Instant updatedAt, String[] roles, boolean isNew) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.email = email;
        this.firstname = firstname;
        this.surname = surname;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles;
        this.isNew = isNew;
    }

    // Getters and Setters
    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() {
        return false;
    }

    public void setId(UUID id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Set<UserRole> getRoles() {
        return Arrays.stream(roles)
                .map(UserRole::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles.stream().map(Enum::name).toArray(String[]::new);
    }
}
