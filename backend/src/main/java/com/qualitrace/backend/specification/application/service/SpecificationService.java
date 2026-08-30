package com.qualitrace.backend.specification.application.service;

import com.qualitrace.backend.component.domain.exception.ComponentDisabledException;
import com.qualitrace.backend.component.domain.exception.ComponentNotFoundException;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.specification.application.dto.SpecificationCreateRequest;
import com.qualitrace.backend.specification.application.dto.SpecificationResponse;
import com.qualitrace.backend.specification.application.dto.SpecificationUpdateRequest;
import com.qualitrace.backend.specification.application.mapper.SpecificationMapper;
import com.qualitrace.backend.specification.domain.exception.SpecificationNotFoundException;
import com.qualitrace.backend.specification.domain.model.Specification;
import com.qualitrace.backend.specification.domain.repository.SpecificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SpecificationService {
    private final SpecificationRepository specificationRepository;
    private final SpecificationMapper specificationMapper;
    private final ComponentRepository componentRepository;

    public SpecificationService(
            SpecificationRepository specificationRepository,
            SpecificationMapper specificationMapper,
            ComponentRepository componentRepository
    ) {
        this.specificationRepository = specificationRepository;
        this.specificationMapper = specificationMapper;
        this.componentRepository = componentRepository;
    }

    @Transactional(readOnly = true)
    public List<SpecificationResponse> getByComponent(Long componentId) {
        List<Specification> specifications = specificationRepository.findByComponent(componentId);
        return specifications.stream()
                .map(specificationMapper::toResponse)
                .toList();
    }

    public SpecificationResponse save(Long componentId, SpecificationCreateRequest request) {
        getEditableComponent(componentId);
        Specification specification = specificationMapper.toDomain(componentId, request, componentRepository);
        Specification saved = specificationRepository.save(specification);

        return specificationMapper.toResponse(saved);
    }

    public SpecificationResponse update(Long id, SpecificationUpdateRequest request) {
        Specification existing = findOrThrow(id);
        getEditableComponent(existing.componentId());
        Specification updated = existing.update(
                request.method(),
                request.min(),
                request.max(),
                componentRepository
        );

        return specificationMapper.toResponse(specificationRepository.save(updated));
    }

    public void delete(Long id) {
        Specification existing = findOrThrow(id);
        getEditableComponent(existing.componentId());

        specificationRepository.save(existing.delete(componentRepository));
    }

    private Specification findOrThrow(Long id) {
        return specificationRepository.findById(id)
                .orElseThrow(() -> new SpecificationNotFoundException(id));
    }

    private Component getEditableComponent(Long componentId) {
        Component component = componentRepository.findById(componentId)
                .orElseThrow(() -> new ComponentNotFoundException(componentId));
        if (component.status() == ComponentStatus.ARCHIVED) {
            throw new ComponentDisabledException(componentId);
        }
        return component;
    }
}
