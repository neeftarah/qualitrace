package com.qualitrace.backend.domain.repository;

import com.qualitrace.backend.domain.model.PageQuery;
import com.qualitrace.backend.domain.model.PageResult;
import com.qualitrace.backend.domain.model.User;
import com.qualitrace.backend.domain.model.UserFilter;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    PageResult<User> findAll(PageQuery pageQuery, UserFilter filter);
    Optional<User> findById(UUID id);
    Optional<User> findByLogin(String login);
    User save(User user);
    boolean existsById(UUID id);
}
