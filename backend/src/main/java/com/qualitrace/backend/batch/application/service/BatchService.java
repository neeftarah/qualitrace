package com.qualitrace.backend.batch.application.service;

import com.qualitrace.backend.analysisresult.application.mapper.AnalysisResultMapper;
import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.analysisresult.domain.repository.AnalysisResultRepository;
import com.qualitrace.backend.batch.application.dto.BatchCreateRequest;
import com.qualitrace.backend.batch.application.dto.BatchDetailResponse;
import com.qualitrace.backend.batch.application.dto.BatchResponse;
import com.qualitrace.backend.batch.application.dto.BatchValidationRequest;
import com.qualitrace.backend.batch.application.mapper.BatchMapper;
import com.qualitrace.backend.batch.domain.exception.BatchNotFoundException;
import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.model.BatchFilter;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.component.application.mapper.ComponentMapper;
import com.qualitrace.backend.component.domain.exception.ComponentNotFoundException;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.deviation.application.dto.DeviationResponse;
import com.qualitrace.backend.deviation.application.mapper.DeviationMapper;
import com.qualitrace.backend.deviation.domain.repository.DeviationRepository;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.specification.application.dto.SpecificationWithResultResponse;
import com.qualitrace.backend.specification.domain.model.Specification;
import com.qualitrace.backend.specification.domain.repository.SpecificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BatchService {
    private final ComponentRepository componentRepository;
    private final DeviationRepository deviationRepository;
    private final AnalysisResultRepository analysisRepository;
    private final SpecificationRepository controlRepository;
    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;
    private final AnalysisResultMapper analysisResultMapper;
    private final DeviationMapper deviationMapper;
    private final SpecificationRepository specificationRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final ComponentMapper componentMapper;

    public BatchService(
            ComponentRepository componentRepository,
            BatchRepository batchRepository,
            BatchMapper batchMapper,
            DeviationRepository deviationRepository,
            AnalysisResultRepository analysisRepository,
            SpecificationRepository controlRepository,
            AnalysisResultMapper analysisResultMapper,
            DeviationMapper deviationMapper,
            SpecificationRepository specificationRepository,
            AnalysisResultRepository analysisResultRepository,
            ComponentMapper componentMapper
    ) {
        this.componentRepository = componentRepository;
        this.batchRepository = batchRepository;
        this.batchMapper = batchMapper;
        this.deviationRepository = deviationRepository;
        this.analysisRepository = analysisRepository;
        this.controlRepository = controlRepository;
        this.analysisResultMapper = analysisResultMapper;
        this.deviationMapper = deviationMapper;
        this.specificationRepository = specificationRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.componentMapper = componentMapper;
    }

    @Transactional(readOnly = true)
    public BatchDetailResponse getOneById(Long id) {
//        Batch batch = batchRepository.findById(id)
//                .orElseThrow(() -> new BatchNotFoundException(id));
//        return batchMapper.toResponse(batch);

        Batch batch = findOrThrow(id);

        List<Specification> specs =
                specificationRepository.findByComponent(batch.component().id());

        Map<Long, AnalysisResult> resultsBySpec = analysisResultRepository.findAllByBatchId(id).stream()
                .collect(Collectors.toMap(AnalysisResult::specificationId, r -> r));

        List<SpecificationWithResultResponse> specResponses = specs.stream()
                .map(spec -> new SpecificationWithResultResponse(
                        spec.id(),
                        spec.name(),
                        spec.method(),
                        spec.unit(),
                        spec.min(),
                        spec.max(),
                        Optional.ofNullable(resultsBySpec.get(spec.id()))
                                .map(analysisResultMapper::toMinimalResponse)
                                .orElse(null)
                ))
                .toList();

        List<DeviationResponse> deviations = deviationRepository.findAllByBatchId(id).stream()
                .map(deviationMapper::toResponse)
                .toList();

        return new BatchDetailResponse(
                batch.id(),
                batch.internalBatchNumber(),
                batch.supplierBatchNumber(),
                batch.expiryDate(),
                batch.receptionDate(),
                batch.status(),
                componentMapper.toResponse(batch.component()),
                specResponses,
                deviations
        );
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
