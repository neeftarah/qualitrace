package com.qualitrace.backend.domain.repository;

import com.qualitrace.backend.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(UUID id);
    Optional<User> findByLogin(String login);
    User save(User user);
}
