package com.qualitrace.backend.deviation.domain.repository;

import com.qualitrace.backend.deviation.domain.model.Deviation;
import com.qualitrace.backend.deviation.domain.type.DeviationStatus;

import java.util.List;
import java.util.Optional;

public interface DeviationRepository {
    Optional<Deviation> findById(Long id);

    List<Deviation> findAllByBatchId(Long id);

    Deviation save(Deviation deviation);

    boolean existsByBatchIdAndStatus(Long id, DeviationStatus deviationStatus);
}
