package com.qualitrace.backend.batch.application.mapper;

import com.qualitrace.backend.batch.application.dto.BatchCreateRequest;
import com.qualitrace.backend.batch.application.dto.BatchResponse;
import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.component.domain.model.Component;
import org.springframework.stereotype.Service;

@Service
public class BatchMapper {

    public BatchResponse toResponse(Batch batch) {
        return new BatchResponse(
                batch.id(),
                batch.component(),
                batch.internalBatchNumber(),
                batch.supplierBatchNumber(),
                batch.expiryDate(),
                batch.receptionDate(),
                batch.status()
        );
    }

    public Batch toDomain(BatchCreateRequest request, Component component, BatchRepository batchRepository) {
        return Batch.createNew(
                component,
                request.supplierBatchNumber(),
                request.expiryDate(),
                batchRepository
        );
    }
}
