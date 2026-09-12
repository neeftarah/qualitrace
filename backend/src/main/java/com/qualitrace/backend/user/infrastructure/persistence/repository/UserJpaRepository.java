package com.qualitrace.backend.user.infrastructure.persistence.repository;

import com.qualitrace.backend.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByLogin(String login);

    @Query(value = """
        SELECT * FROM users u
        WHERE (:login IS NULL OR u.login ILIKE CONCAT('%', CAST(:login AS text), '%'))
          AND (:email IS NULL OR u.email ILIKE CONCAT('%', CAST(:email AS text), '%'))
          AND (:firstname IS NULL OR u.firstname ILIKE CONCAT('%', CAST(:firstname AS text), '%'))
          AND (:surname IS NULL OR u.surname ILIKE CONCAT('%', CAST(:surname AS text), '%'))
          AND (:status IS NULL OR u.status = CAST(:status AS text))
          AND (:role IS NULL OR CAST(:role AS user_role) = ANY(u.roles))
          AND (CAST(:fromCreationDate AS timestamp) IS NULL OR u.created_at >= :fromCreationDate)
          AND (CAST(:toCreationDate AS timestamp) IS NULL OR u.created_at <= :toCreationDate)
          AND (CAST(:fromUpdateDate AS timestamp) IS NULL OR u.updated_at >= :fromUpdateDate)
          AND (CAST(:toUpdateDate AS timestamp) IS NULL OR u.updated_at <= :toUpdateDate)
        """,
            countQuery = """
        SELECT count(*) FROM users u
        WHERE (:login IS NULL OR u.login ILIKE CONCAT('%', CAST(:login AS text), '%'))
          AND (:email IS NULL OR u.email ILIKE CONCAT('%', CAST(:email AS text), '%'))
          AND (:firstname IS NULL OR u.firstname ILIKE CONCAT('%', CAST(:firstname AS text), '%'))
          AND (:surname IS NULL OR u.surname ILIKE CONCAT('%', CAST(:surname AS text), '%'))
          AND (:status IS NULL OR u.status = CAST(:status AS text))
              AND (:role IS NULL OR CAST(:role AS user_role) = ANY(u.roles))
          AND (CAST(:fromCreationDate AS timestamp) IS NULL OR u.created_at >= :fromCreationDate)
          AND (CAST(:toCreationDate AS timestamp) IS NULL OR u.created_at <= :toCreationDate)
          AND (CAST(:fromUpdateDate AS timestamp) IS NULL OR u.updated_at >= :fromUpdateDate)
          AND (CAST(:toUpdateDate AS timestamp) IS NULL OR u.updated_at <= :toUpdateDate)
        """,
            nativeQuery = true)
    Page<UserEntity> search(
            @Param("login") String login,
            @Param("email") String email,
            @Param("firstname") String firstname,
            @Param("surname") String surname,
            @Param("status") String status,
            @Param("role") String role,
            @Param("fromCreationDate") Instant fromCreationDate,
            @Param("toCreationDate") Instant toCreationDate,
            @Param("fromUpdateDate") Instant fromUpdateDate,
            @Param("toUpdateDate") Instant toUpdateDate,
            Pageable pageable
    );
}
