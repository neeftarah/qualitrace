package com.qualitrace.backend.infrastructure.persistence.repository;

import com.qualitrace.backend.infrastructure.persistence.entity.ComponentEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ComponentJpaRepository extends JpaRepository<ComponentEntity, Long> {
    @Override
    @NullMarked
    @EntityGraph(attributePaths = {"supplier"})
    Optional<ComponentEntity> findById(Long id);

    @EntityGraph(attributePaths = {"supplier"})
    Optional<ComponentEntity> findByReference(String reference);

    @EntityGraph(attributePaths = {"supplier"})
    Optional<ComponentEntity> findByName(String name);

    @EntityGraph(attributePaths = {"supplier"})
    @Query("""
            SELECT c FROM ComponentEntity c
            WHERE (:type IS NULL OR LOWER(CAST(c.type AS string)) LIKE :type)
            AND (:reference IS NULL OR LOWER(c.reference) LIKE :reference)
            AND (:name IS NULL OR LOWER(c.name) LIKE :name)
            AND (:status IS NULL OR LOWER(CAST(c.status AS string)) LIKE :status)
            AND (:supplierId IS NULL OR c.supplier.id = :supplierId)
            """)
    Page<ComponentEntity> search(
            @Param("type") String type,
            @Param("reference") String reference,
            @Param("name") String name,
            @Param("status") String status,
            @Param("supplierId") Long supplierId,
            Pageable pageable
    );
}
