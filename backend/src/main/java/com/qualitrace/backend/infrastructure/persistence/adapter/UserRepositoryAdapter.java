package com.qualitrace.backend.infrastructure.persistence.adapter;

import com.qualitrace.backend.domain.model.User;
import com.qualitrace.backend.domain.repository.UserRepository;
import com.qualitrace.backend.infrastructure.persistence.entity.UserEntity;
import com.qualitrace.backend.infrastructure.persistence.repository.UserJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return jpaRepository.findByLogin(login).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        boolean isNew = !jpaRepository.existsById(user.getId());
        UserEntity entity = toEntity(user, isNew);
        UserEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private User toDomain(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getLogin(),
            entity.getPassword(),
            entity.getEmail(),
            entity.getFirstname(),
            entity.getSurname(),
            entity.getStatus(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private UserEntity toEntity(User user, boolean isNew) {
        return new UserEntity(
            user.getId(),
            user.getLogin(),
            user.getPassword(),
            user.getEmail(),
            user.getFirstname(),
            user.getSurname(),
            user.getStatus(),
            user.getVersion(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            isNew
        );
    }
}
