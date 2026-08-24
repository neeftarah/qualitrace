package com.qualitrace.backend.batch.domain.repository;

import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.model.BatchFilter;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;

import java.time.Instant;
import java.util.Optional;

public interface BatchRepository {
    Optional<Batch> findById(Long id);

    PageResult<Batch> findAll(PageQuery pageQuery, BatchFilter filter);

    Batch save(Batch component);

    String nextInternalReferenceNumber(ComponentType type, Instant receptionDate);
}
