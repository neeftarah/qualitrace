package com.qualitrace.backend.analysisresult.infrastructure.persistence.repository;

import com.qualitrace.backend.analysisresult.infrastructure.persistence.entity.AnalysisResultEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultJpaRepository extends JpaRepository<AnalysisResultEntity, Long> {
    @Override
    @NullMarked
    @EntityGraph(attributePaths = {"users"})
    Optional<AnalysisResultEntity> findById(Long id);

    @EntityGraph(attributePaths = {"users"})
    List<AnalysisResultEntity> findAllByBatchId(Long componentId);
}
