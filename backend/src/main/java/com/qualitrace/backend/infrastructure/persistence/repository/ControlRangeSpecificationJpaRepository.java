package com.qualitrace.backend.infrastructure.persistence.repository;

import com.qualitrace.backend.domain.type.ControlRangeSpecificationStatus;
import com.qualitrace.backend.infrastructure.persistence.entity.ControlRangeSpecificationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ControlRangeSpecificationJpaRepository extends JpaRepository<ControlRangeSpecificationEntity, Long> {

    @EntityGraph(attributePaths = {"component"})
    Optional<ControlRangeSpecificationEntity> findByComponentIdAndStatusNot(Long componentId, ControlRangeSpecificationStatus status);

    boolean existsByComponentIdAndStatusNot(Long componentId, ControlRangeSpecificationStatus status);
}
