package com.qualitrace.backend.audittrail.infrastructure.persistence.repository;

import com.qualitrace.backend.audittrail.infrastructure.persistence.entity.AuditTrailEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface AuditTrailJpaRepository extends JpaRepository<AuditTrailEntity, Long> {
    @EntityGraph(attributePaths = {"author"})
    @Query("""
        SELECT at FROM AuditTrailEntity at
        LEFT JOIN at.author author
        WHERE (:author_id IS NULL OR at.author.id = :author_id)
        AND (:event IS NULL OR at.event = :event)
        AND (:entity_type IS NULL OR at.entityType = :entity_type)
        AND (:entity_id IS NULL OR at.entityId = :entity_id)
        AND (CAST(:fromDate AS timestamp) IS NULL OR at.timestamp >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR at.timestamp <= :toDate)
        AND (:content IS NULL OR (
            CAST(at.previousData AS string) LIKE :content
            OR CAST(at.changedData AS string) LIKE :content))
        ORDER BY at.timestamp DESC
    """)
    Page<AuditTrailEntity> search(
            @Param("author_id") UUID author_id,
            @Param("event") String event,
            @Param("entity_type") String entity_type,
            @Param("entity_id") String entity_id,
            @Param("content") String content,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );
}
