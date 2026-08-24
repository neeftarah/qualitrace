package com.qualitrace.backend.deviation.infrastructure.persistence.repository;

import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import com.qualitrace.backend.deviation.infrastructure.persistence.entity.DeviationEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviationJpaRepository extends JpaRepository<DeviationEntity, Long> {
    @Override
    @NullMarked
    Optional<DeviationEntity> findById(Long id);

    List<DeviationEntity> findAllByBatchId(Long componentId);

    boolean existsByBatchIdAndStatus(Long id, DeviationStatus deviationStatus);
}
