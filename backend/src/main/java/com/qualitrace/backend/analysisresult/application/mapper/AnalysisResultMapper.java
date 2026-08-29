package com.qualitrace.backend.analysisresult.application.mapper;

import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultCreateRequest;
import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultResponse;
import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.user.application.mapper.UserMapper;
import com.qualitrace.backend.user.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class AnalysisResultMapper {
    private final BatchRepository batchRepository;
    private final UserMapper userMapper;

    public AnalysisResultMapper(BatchRepository batchRepository, UserMapper userMapper) {
        this.batchRepository = batchRepository;
        this.userMapper = userMapper;
    }

    public AnalysisResultResponse toResponse(AnalysisResult AnalysisResult) {
        return new AnalysisResultResponse(
                AnalysisResult.id(),
                AnalysisResult.batchId(),
                AnalysisResult.specificationId(),
                AnalysisResult.value(),
                AnalysisResult.createdAt(),
                userMapper.toResponse(AnalysisResult.createdBy())
        );
    }

    public AnalysisResult toDomain(
            Long batchId,
            User user,
            AnalysisResultCreateRequest request
    ) {
        return AnalysisResult.createNew(
                batchId,
                request.specificationId(),
                request.value(),
                user,
                batchRepository
        );
    }
}
