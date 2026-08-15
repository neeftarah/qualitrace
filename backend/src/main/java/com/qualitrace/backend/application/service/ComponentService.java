package com.qualitrace.backend.application.service;

import com.qualitrace.backend.application.dto.ComponentCreateRequest;
import com.qualitrace.backend.application.dto.ComponentResponse;
import com.qualitrace.backend.application.dto.ComponentUpdateRequest;
import com.qualitrace.backend.application.mapper.ComponentMapper;
import com.qualitrace.backend.domain.exception.ComponentNotFoundException;
import com.qualitrace.backend.domain.exception.SupplierNotFoundException;
import com.qualitrace.backend.domain.model.*;
import com.qualitrace.backend.domain.repository.ComponentRepository;
import com.qualitrace.backend.domain.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComponentService {
    @Autowired
    private final SupplierRepository supplierRepository;

    private final ComponentRepository componentRepository;
    private final ComponentMapper componentMapper;

    public ComponentService(SupplierRepository supplierRepository, ComponentRepository componentRepository, ComponentMapper componentMapper) {
        this.supplierRepository = supplierRepository;
        this.componentRepository = componentRepository;
        this.componentMapper = componentMapper;
    }

    @Transactional(readOnly = true)
    public ComponentResponse getOneById(Long id) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new ComponentNotFoundException(id));
        return componentMapper.toResponse(component);
    }

    @Transactional(readOnly = true)
    public PageResult<ComponentResponse> getAll(PageQuery pageQuery, ComponentFilter filter) {
        return componentRepository.findAll(pageQuery, filter)
                .map(componentMapper::toResponse);
    }

    public ComponentResponse save(ComponentCreateRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new SupplierNotFoundException(request.supplierId()));

        Component component = componentMapper.toDomain(request, supplier);
        Component saved = componentRepository.save(component);
        return componentMapper.toResponse(saved);
    }

    public ComponentResponse update(Long id, ComponentUpdateRequest request) {
        Component existing = findOrThrow(id);
        Component updated = existing.update(
                request.name()
        );

        return componentMapper.toResponse(componentRepository.save(updated));
    }

    public ComponentResponse draft(Long id) {
        Component existing = findOrThrow(id);
        return componentMapper.toResponse(componentRepository.save(existing.draft()));
    }

    public ComponentResponse archive(Long id) {
        Component existing = findOrThrow(id);
        return componentMapper.toResponse(componentRepository.save(existing.archive()));
    }

    public ComponentResponse activate(Long id) {
        Component existing = findOrThrow(id);
        return componentMapper.toResponse(componentRepository.save(existing.activate()));
    }

    private Component findOrThrow(Long id) {
        return componentRepository.findById(id)
                .orElseThrow(() -> new ComponentNotFoundException(id));
    }
}