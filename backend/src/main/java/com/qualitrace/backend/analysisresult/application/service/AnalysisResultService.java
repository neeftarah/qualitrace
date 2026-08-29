package com.qualitrace.backend.analysisresult.application.service;

import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultCreateRequest;
import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultResponse;
import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultUpdateRequest;
import com.qualitrace.backend.analysisresult.application.mapper.AnalysisResultMapper;
import com.qualitrace.backend.analysisresult.domain.exception.AnalysisResultNotFoundException;
import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.analysisresult.domain.repository.AnalysisResultRepository;
import com.qualitrace.backend.batch.domain.exception.BatchNotFoundException;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.shared.infrastructure.security.QualitracePrincipal;
import com.qualitrace.backend.user.domain.exception.UserNotFoundException;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AnalysisResultService {
    private final AnalysisResultRepository analysisResultRepository;
    private final AnalysisResultMapper analysisResultMapper;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;

    public AnalysisResultService(
            AnalysisResultRepository analysisResultRepository,
            AnalysisResultMapper AnalysisResultMapper, BatchRepository batchRepository, UserRepository userRepository
    ) {
        this.analysisResultRepository = analysisResultRepository;
        this.analysisResultMapper = AnalysisResultMapper;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AnalysisResultResponse> getByBatch(Long batchId) {
        List<AnalysisResult> AnalysisResults = analysisResultRepository.findAllByBatchId(batchId);
        return AnalysisResults.stream()
                .map(analysisResultMapper::toResponse)
                .toList();
    }

    public AnalysisResultResponse save(Long batchId, QualitracePrincipal principal, AnalysisResultCreateRequest request) {
        batchRepository.findById(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        User currentUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UserNotFoundException(principal.getId()));

        AnalysisResult saved = analysisResultRepository.save(
                analysisResultMapper.toDomain(batchId, currentUser, request)
        );

        return analysisResultMapper.toResponse(saved);
    }

    public AnalysisResultResponse update(Long id, AnalysisResultUpdateRequest request) {
        AnalysisResult existing = findOrThrow(id);
        AnalysisResult updated = existing.update(
                request.value(),
                batchRepository
        );

        return analysisResultMapper.toResponse(analysisResultRepository.save(updated));
    }

    private AnalysisResult findOrThrow(Long id) {
        return analysisResultRepository.findById(id)
                .orElseThrow(() -> new AnalysisResultNotFoundException(id));
    }
}
