package com.qualitrace.backend.application.mapper;

import com.qualitrace.backend.application.dto.ControlRangeSpecificationCreateRequest;
import com.qualitrace.backend.application.dto.ControlRangeSpecificationResponse;
import com.qualitrace.backend.domain.model.ControlRangeSpecification;
import org.springframework.stereotype.Service;

@Service
public class ControlRangeSpecificationMapper {

    public ControlRangeSpecificationResponse toResponse(ControlRangeSpecification controlRangeSpecification) {
        return new ControlRangeSpecificationResponse(
                controlRangeSpecification.id(),
                controlRangeSpecification.name(),
                controlRangeSpecification.method(),
                controlRangeSpecification.unit(),
                controlRangeSpecification.min(),
                controlRangeSpecification.max(),
                controlRangeSpecification.componentId()
        );
    }

    public ControlRangeSpecification toDomain(Long componentId, ControlRangeSpecificationCreateRequest request) {
        return ControlRangeSpecification.createNew(
                request.name(),
                request.method(),
                request.unit(),
                request.min(),
                request.max(),
                componentId
        );
    }
}
