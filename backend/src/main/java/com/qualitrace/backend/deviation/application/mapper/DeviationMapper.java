package com.qualitrace.backend.deviation.application.mapper;

import com.qualitrace.backend.deviation.domain.model.Deviation;
import com.qualitrace.backend.deviation.application.dto.DeviationCreateRequest;
import com.qualitrace.backend.deviation.application.dto.DeviationResponse;
import org.springframework.stereotype.Service;

@Service
public class DeviationMapper {

    public DeviationResponse toResponse(Deviation Deviation) {
        return new DeviationResponse(
                Deviation.id(),
                Deviation.code(),
                Deviation.status(),
                Deviation.comment(),
                Deviation.batchId()
        );
    }

    public Deviation toDomain(
            Long batchId,
            DeviationCreateRequest request
    ) {
        return Deviation.createNew(
                batchId,
                request.code(),
                request.status(),
                request.comment()
        );
    }
}
