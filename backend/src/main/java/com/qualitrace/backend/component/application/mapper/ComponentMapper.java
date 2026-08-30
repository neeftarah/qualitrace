package com.qualitrace.backend.component.application.mapper;

import com.qualitrace.backend.component.application.dto.ComponentCreateRequest;
import com.qualitrace.backend.component.application.dto.ComponentResponse;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.supplier.application.mapper.SupplierMapper;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import org.springframework.stereotype.Service;

@Service
public class ComponentMapper {
    private final SupplierMapper supplierMapper;

    public ComponentMapper(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    public ComponentResponse toResponse(Component component) {
        return new ComponentResponse(
                component.id(),
                component.type(),
                component.reference(),
                component.name(),
                component.availableFrom(),
                component.status(),
                supplierMapper.toResponse(component.supplier())
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
