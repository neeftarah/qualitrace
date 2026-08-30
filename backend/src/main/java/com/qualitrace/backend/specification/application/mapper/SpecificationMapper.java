package com.qualitrace.backend.specification.application.mapper;

import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.specification.application.dto.SpecificationCreateRequest;
import com.qualitrace.backend.specification.application.dto.SpecificationResponse;
import com.qualitrace.backend.specification.domain.model.Specification;
import org.springframework.stereotype.Service;

@Service
public class SpecificationMapper {

    public SpecificationResponse toResponse(Specification specification) {
        return new SpecificationResponse(
                specification.id(),
                specification.name(),
                specification.method(),
                specification.unit(),
                specification.min(),
                specification.max(),
                specification.componentId()
        );
    }

    public Specification toDomain(
            Long componentId,
            SpecificationCreateRequest request,
            ComponentRepository componentRepository
    ) {
        return Specification.createNew(
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
