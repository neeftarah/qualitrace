package com.qualitrace.backend.infrastructure.persistence.repository;

import com.qualitrace.backend.infrastructure.persistence.entity.AuditTrailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditTrailJpaRepository extends JpaRepository<AuditTrailEntity, Long> {
}
