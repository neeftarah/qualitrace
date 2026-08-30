package com.qualitrace.backend.specification.infrastructure.persistence.repository;

import com.qualitrace.backend.specification.domain.type.SpecificationStatus;
import com.qualitrace.backend.specification.infrastructure.persistence.entity.SpecificationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecificationJpaRepository extends JpaRepository<SpecificationEntity, Long> {

    @EntityGraph(attributePaths = {"component"})
    List<SpecificationEntity> findByComponentIdAndStatusNot(Long componentId, SpecificationStatus status);

    boolean existsByComponentIdAndStatusNot(Long componentId, SpecificationStatus status);
}
