package com.qualitrace.backend.controls.domain.repository;

import com.qualitrace.backend.controls.domain.model.ControlRangeSpecification;

import java.util.List;
import java.util.Optional;

public interface ControlRangeSpecificationRepository {
    Optional<ControlRangeSpecification> findById(Long id);

    List<ControlRangeSpecification> findByComponent(Long componentId);

    boolean existsActiveSpecForComponent(Long componentId);

    ControlRangeSpecification save(ControlRangeSpecification controlRangeSpecification);
}
