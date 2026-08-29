package com.qualitrace.backend.deviation.application.service;

import com.qualitrace.backend.batch.domain.exception.BatchNotFoundException;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.deviation.application.dto.DeviationCreateRequest;
import com.qualitrace.backend.deviation.application.dto.DeviationResponse;
import com.qualitrace.backend.deviation.application.dto.DeviationUpdateRequest;
import com.qualitrace.backend.deviation.application.mapper.DeviationMapper;
import com.qualitrace.backend.deviation.domain.exception.DeviationNotFoundException;
import com.qualitrace.backend.deviation.domain.model.Deviation;
import com.qualitrace.backend.deviation.domain.repository.DeviationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeviationService {
    private final DeviationRepository deviationRepository;
    private final DeviationMapper deviationMapper;
    private final BatchRepository batchRepository;

    public DeviationService(
            DeviationRepository deviationRepository,
            DeviationMapper DeviationMapper, BatchRepository batchRepository
    ) {
        this.deviationRepository = deviationRepository;
        this.deviationMapper = DeviationMapper;
        this.batchRepository = batchRepository;
    }

    @Transactional(readOnly = true)
    public List<DeviationResponse> getByBatch(Long batchId) {
        List<Deviation> Deviations = deviationRepository.findAllByBatchId(batchId);
        return Deviations.stream()
                .map(deviationMapper::toResponse)
                .toList();
    }

    public DeviationResponse save(Long batchId, DeviationCreateRequest request) {
        batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        Deviation saved = deviationRepository.save(deviationMapper.toDomain(batchId, request, batchRepository));

        return deviationMapper.toResponse(saved);
    }

    public DeviationResponse update(Long id, DeviationUpdateRequest request) {
        Deviation existing = findOrThrow(id);
        Deviation updated = existing.update(
                request.comment(),
                batchRepository
        );

        return deviationMapper.toResponse(deviationRepository.save(updated));
    }

    public DeviationResponse open(Long id) {
        Deviation existing = findOrThrow(id);

        return deviationMapper.toResponse(deviationRepository.save(existing.open()));
    }

    public DeviationResponse close(Long id) {
        Deviation existing = findOrThrow(id);

        return deviationMapper.toResponse(deviationRepository.save(existing.close()));
    }

    private Deviation findOrThrow(Long id) {
        return deviationRepository.findById(id)
                .orElseThrow(() -> new DeviationNotFoundException(id));
    }
}
