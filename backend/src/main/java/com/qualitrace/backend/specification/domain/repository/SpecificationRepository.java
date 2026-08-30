package com.qualitrace.backend.specification.domain.repository;

import com.qualitrace.backend.specification.domain.model.Specification;

import java.util.List;
import java.util.Optional;

public interface SpecificationRepository {
    Optional<Specification> findById(Long id);

    List<Specification> findByComponent(Long componentId);

    boolean existsActiveSpecForComponent(Long componentId);

    Specification save(Specification specification);
}
