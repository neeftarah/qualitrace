package com.qualitrace.backend.application.mapper;

import com.qualitrace.backend.application.dto.ComponentCreateRequest;
import com.qualitrace.backend.application.dto.ComponentResponse;
import com.qualitrace.backend.domain.model.Component;
import com.qualitrace.backend.domain.model.Supplier;
import org.springframework.stereotype.Service;

@Service
public class ComponentMapper {

    public ComponentResponse toResponse(Component component) {
        return new ComponentResponse(
                component.id(),
                component.type(),
                component.reference(),
                component.name(),
                component.status(),
                component.supplier()
        );
    }

    public Component toDomain(ComponentCreateRequest request, Supplier supplier) {
        return Component.createNew(
                request.type(),
                request.reference(),
                request.name(),
                supplier
        );
    }
}
