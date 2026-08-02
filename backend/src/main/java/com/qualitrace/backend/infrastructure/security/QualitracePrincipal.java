package com.qualitrace.backend.infrastructure.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@NullMarked
public class QualitracePrincipal implements UserDetails {
    private final UUID id;
    private final String login;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean locked;
    private final boolean disabled;

    public QualitracePrincipal(UUID id, String login, String password, Collection<? extends GrantedAuthority> authorities, boolean locked, boolean disabled) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.authorities = authorities;
        this.locked = locked;
        this.disabled = disabled;
    }

    public UUID getId() { return id; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isEnabled() {
        return !this.disabled;
    }
}