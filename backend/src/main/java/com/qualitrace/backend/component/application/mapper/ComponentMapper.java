package com.qualitrace.backend.component.application.mapper;

import com.qualitrace.backend.component.application.dto.ComponentCreateRequest;
import com.qualitrace.backend.component.application.dto.ComponentResponse;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import org.springframework.stereotype.Service;

@Service
public class ComponentMapper {

    public ComponentResponse toResponse(Component component) {
        return new ComponentResponse(
                component.id(),
                component.type(),
                component.reference(),
                component.name(),
                component.availableFrom(),
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
