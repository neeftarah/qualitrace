package com.qualitrace.backend.user.infrastructure.persistence.adapter;

import com.qualitrace.backend.user.domain.repository.UserRepository;
import com.qualitrace.backend.user.infrastructure.persistence.entity.UserEntity;
import com.qualitrace.backend.user.infrastructure.persistence.repository.UserJpaRepository;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.shared.domain.model.SortQuery;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.domain.model.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PageResult<User> findAll(PageQuery pageQuery, UserFilter filter) {
        Sort sort = Sort.by(pageQuery.sort().stream()
                .map(s -> new Sort.Order(
                        s.direction() == SortQuery.Direction.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        s.field()))
                .toList());
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), sort);

        Page<UserEntity> page = jpaRepository.search(
                filter.login(),
                filter.email(),
                filter.firstname(),
                filter.surname(),
                filter.status() != null ? filter.status().name() : null,
                filter.role() != null ? filter.role().name() : null,
                pageable
        );

        return new PageResult<>(
                page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
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
        UserEntity entity = jpaRepository.findById(user.id())
                .map(existing -> applyChanges(existing, user))
                .orElseGet(() -> toNewEntity(user));
        UserEntity saved = jpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    /**
     * Construit une entité du domaine à partir d'une entité JPA
     * @param entity Objet JPA
     *
     * @return Objet du domaine (User)
     */
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
            entity.getUpdatedAt(),
            entity.getRoles()
        );
    }


    private UserEntity applyChanges(UserEntity entity, User user) {
        entity.setLogin(user.login());
        entity.setPassword(user.password());
        entity.setEmail(user.email());
        entity.setFirstname(user.firstname());
        entity.setSurname(user.surname());
        entity.setStatus(user.status());
        entity.setUpdatedAt(user.updatedAt());
        entity.setRoles(user.roles());
        return entity;
    }

    private UserEntity toNewEntity(User user) {
        return new UserEntity(
                user.id(),
                user.login(),
                user.password(),
                user.email(),
                user.firstname(),
                user.surname(),
                user.status(),
                user.version(),
                user.createdAt(),
                user.updatedAt(),
                user.roles().stream().map(Enum::name).toArray(String[]::new),
                true
        );
    }
}
