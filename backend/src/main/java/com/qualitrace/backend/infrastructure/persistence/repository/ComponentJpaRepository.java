package com.qualitrace.backend.infrastructure.persistence.repository;

import com.qualitrace.backend.infrastructure.persistence.entity.ComponentEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ComponentJpaRepository extends JpaRepository<ComponentEntity, Long> {
    Optional<ComponentEntity> findByReference(String reference);
    Optional<ComponentEntity> findByName(String name);

    @Override
    @NullMarked
    @EntityGraph(attributePaths = {"supplier"})
    List<ComponentEntity> findAll();

    @Query(value = """
        SELECT * FROM components s
        WHERE (:type IS NULL OR s.type ILIKE CONCAT('%', CAST(:type AS text), '%'))
          AND (:reference IS NULL OR s.reference ILIKE CONCAT('%', CAST(:reference AS text), '%'))
          AND (:name IS NULL OR s.name ILIKE CONCAT('%', CAST(:name AS text), '%'))
          AND (:status IS NULL OR s.status ILIKE CONCAT('%', CAST(:status AS text), '%'))
          AND (:supplier IS NULL OR s.supplier_id = :supplier)
        """,
            countQuery = """
        SELECT count(*) FROM components s
        WHERE (:type IS NULL OR s.type ILIKE CONCAT('%', CAST(:type AS text), '%'))
          AND (:reference IS NULL OR s.reference ILIKE CONCAT('%', CAST(:reference AS text), '%'))
          AND (:name IS NULL OR s.name ILIKE CONCAT('%', CAST(:name AS text), '%'))
          AND (:status IS NULL OR s.status ILIKE CONCAT('%', CAST(:status AS text), '%'))
          AND (:supplier IS NULL OR s.supplier_id = :supplier)
        """,
            nativeQuery = true)
    Page<ComponentEntity> search(
            @Param("type") String type,
            @Param("reference") String reference,
            @Param("name") String name,
            @Param("status") String status,
            @Param("supplier") Long supplier,
            Pageable pageable
    );
}
