package com.qualitrace.backend.batch.infrastructure.persistence.repository;

import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.batch.infrastructure.persistence.entity.BatchEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface BatchJpaRepository extends JpaRepository<BatchEntity, Long> {
    @Override
    @NullMarked
    Optional<BatchEntity> findById(Long id);

    @Query("""
            SELECT b FROM BatchEntity b
            WHERE (:internalBatchNumber IS NULL OR b.internalBatchNumber = :internalBatchNumber)
            AND (:supplierId IS NULL OR b.supplier.id = :supplierId)
            AND (:supplierBatchNumber IS NULL OR b.supplierBatchNumber = :supplierBatchNumber)
            AND (CAST(:expiryFromDate AS timestamp) IS NULL OR b.expiryDate >= :expiryFromDate)
            AND (CAST(:expiryToDate AS timestamp) IS NULL OR b.expiryDate <= :expiryToDate)
            AND (CAST(:receptionFromDate AS timestamp) IS NULL OR b.receptionDate >= :receptionFromDate)
            AND (CAST(:receptionToDate AS timestamp) IS NULL OR b.receptionDate <= :receptionToDate)
            AND (:status IS NULL OR b.status = :status)
            AND (:validatedBy IS NULL OR b.validatedBy.id = :validatedBy)
            AND (CAST(:validationFromDate AS timestamp) IS NULL OR b.validatedAt >= :validationFromDate)
            AND (CAST(:validationToDate AS timestamp) IS NULL OR b.validatedAt <= :validationToDate)
            """)
    Page<BatchEntity> search(
            @Param("internalBatchNumber") String internalBatchNumber,
            @Param("supplierId") Long supplierId,
            @Param("supplierBatchNumber") String supplierBatchNumber,
            @Param("expiryFromDate") Instant expiryFromDate,
            @Param("expiryToDate") Instant expiryToDate,
            @Param("receptionFromDate") Instant receptionFromDate,
            @Param("receptionToDate") Instant receptionToDate,
            @Param("status") BatchStatus status,
            @Param("validatedBy") UUID validatedBy,
            @Param("validationFromDate") Instant validationFromDate,
            @Param("validationToDate") Instant validationToDate,
            Pageable pageable
    );

    // com.qualitrace.backend.batch.infrastructure.persistence.repository.BatchJpaRepository.java

    @Query("""
                SELECT MAX(b.internalBatchNumber)
                FROM BatchEntity b 
                WHERE b.internalBatchNumber LIKE CONCAT(:prefix, '%')
            """)
    Optional<String> findMaxInternalReferenceByPrefix(@Param("prefix") String prefix);
}
