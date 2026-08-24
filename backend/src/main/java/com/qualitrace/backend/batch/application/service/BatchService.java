package com.qualitrace.backend.batch.application.service;

import com.qualitrace.backend.analysisresult.domain.repository.AnalysisResultRepository;
import com.qualitrace.backend.batch.application.dto.BatchCreateRequest;
import com.qualitrace.backend.batch.application.dto.BatchResponse;
import com.qualitrace.backend.batch.application.dto.BatchValidationRequest;
import com.qualitrace.backend.batch.application.mapper.BatchMapper;
import com.qualitrace.backend.batch.domain.exception.BatchNotFoundException;
import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.model.BatchFilter;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.component.domain.exception.ComponentNotFoundException;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.controls.domain.repository.ControlRangeSpecificationRepository;
import com.qualitrace.backend.deviation.domain.repository.DeviationRepository;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BatchService {
    private final ComponentRepository componentRepository;
    private final DeviationRepository deviationRepository;
    private final AnalysisResultRepository analysisRepository;
    private final ControlRangeSpecificationRepository controlRepository;
    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;

    public BatchService(
            ComponentRepository componentRepository,
            BatchRepository batchRepository,
            BatchMapper batchMapper,
            DeviationRepository deviationRepository,
            AnalysisResultRepository analysisRepository,
            ControlRangeSpecificationRepository controlRepository
    ) {
        this.componentRepository = componentRepository;
        this.batchRepository = batchRepository;
        this.batchMapper = batchMapper;
        this.deviationRepository = deviationRepository;
        this.analysisRepository = analysisRepository;
        this.controlRepository = controlRepository;
    }

    @Transactional(readOnly = true)
    public BatchResponse getOneById(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new BatchNotFoundException(id));
        return batchMapper.toResponse(batch);
    }

    @Transactional(readOnly = true)
    public PageResult<BatchResponse> getAll(PageQuery pageQuery, BatchFilter filter) {
        return batchRepository.findAll(pageQuery, filter)
                .map(batchMapper::toResponse);
    }

    public BatchResponse save(BatchCreateRequest request) {
        Component component = componentRepository.findById(request.componentId())
                .orElseThrow(() -> new ComponentNotFoundException(request.componentId()));

        Batch batch = batchMapper.toDomain(request, component, batchRepository);
        Batch saved = batchRepository.save(batch);
        return batchMapper.toResponse(saved);
    }

    public BatchResponse validate(Long id, BatchValidationRequest request) {
        Batch existing = findOrThrow(id);
        return batchMapper.toResponse(batchRepository.save(existing.validate(
                request.accept(),
                deviationRepository,
                analysisRepository,
                controlRepository
        )));
    }

    public BatchResponse use(Long id) {
        Batch existing = findOrThrow(id);
        return batchMapper.toResponse(batchRepository.save(existing.use()));
    }

    public BatchResponse destroy(Long id) {
        Batch existing = findOrThrow(id);
        return batchMapper.toResponse(batchRepository.save(existing.destroy()));
    }

    private Batch findOrThrow(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new BatchNotFoundException(id));
    }
}
