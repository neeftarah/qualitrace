package com.qualitrace.backend.analysisresult.domain.repository;

import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository {
    Optional<AnalysisResult> findById(Long id);

    List<AnalysisResult> findAllByBatchId(Long id);

    AnalysisResult save(AnalysisResult result);
}
