package com.qualitrace.backend.supplier.infrastructure.persistence.repository;

import com.qualitrace.backend.supplier.infrastructure.persistence.entity.SupplierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplierJpaRepository extends JpaRepository<SupplierEntity, Long> {
    Optional<SupplierEntity> findByCode(String code);
    Optional<SupplierEntity> findByName(String name);

    @Query(value = """
        SELECT * FROM suppliers s
        WHERE (:code IS NULL OR s.code ILIKE CONCAT('%', CAST(:code AS text), '%'))
          AND (:name IS NULL OR s.name ILIKE CONCAT('%', CAST(:name AS text), '%'))
          AND (:status IS NULL OR s.status ILIKE CONCAT('%', CAST(:status AS text), '%'))
        """,
            countQuery = """
        SELECT count(*) FROM suppliers s
        WHERE (:code IS NULL OR s.code ILIKE CONCAT('%', CAST(:code AS text), '%'))
          AND (:name IS NULL OR s.name ILIKE CONCAT('%', CAST(:name AS text), '%'))
          AND (:status IS NULL OR s.status ILIKE CONCAT('%', CAST(:status AS text), '%'))
        """,
            nativeQuery = true)
    Page<SupplierEntity> search(
            @Param("code") String code,
            @Param("name") String name,
            @Param("status") String status,
            Pageable pageable
    );
}
