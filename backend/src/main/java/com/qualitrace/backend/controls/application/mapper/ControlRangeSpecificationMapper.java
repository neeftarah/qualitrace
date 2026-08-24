package com.qualitrace.backend.controls.application.mapper;

import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.controls.application.dto.ControlRangeSpecificationCreateRequest;
import com.qualitrace.backend.controls.application.dto.ControlRangeSpecificationResponse;
import com.qualitrace.backend.controls.domain.model.ControlRangeSpecification;
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

    public ControlRangeSpecification toDomain(
            Long componentId,
            ControlRangeSpecificationCreateRequest request,
            ComponentRepository componentRepository
    ) {
        return ControlRangeSpecification.createNew(
                request.name(),
                request.method(),
                request.unit(),
                request.min(),
                request.max(),
                componentId,
                componentRepository
        );
    }
}
