package com.qualitrace.backend.controls.application.service;

import com.qualitrace.backend.component.domain.exception.ComponentDisabledException;
import com.qualitrace.backend.component.domain.exception.ComponentNotFoundException;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.controls.application.dto.ControlRangeSpecificationCreateRequest;
import com.qualitrace.backend.controls.application.dto.ControlRangeSpecificationResponse;
import com.qualitrace.backend.controls.application.dto.ControlRangeSpecificationUpdateRequest;
import com.qualitrace.backend.controls.application.mapper.ControlRangeSpecificationMapper;
import com.qualitrace.backend.controls.domain.exception.ControlRangeSpecificationNotFoundException;
import com.qualitrace.backend.controls.domain.model.ControlRangeSpecification;
import com.qualitrace.backend.controls.domain.repository.ControlRangeSpecificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ControlRangeSpecificationService {
    private final ControlRangeSpecificationRepository controlRangeSpecificationRepository;
    private final ControlRangeSpecificationMapper controlRangeSpecificationMapper;
    private final ComponentRepository componentRepository;

    public ControlRangeSpecificationService(
            ControlRangeSpecificationRepository controlRangeSpecificationRepository,
            ControlRangeSpecificationMapper controlRangeSpecificationMapper,
            ComponentRepository componentRepository
    ) {
        this.controlRangeSpecificationRepository = controlRangeSpecificationRepository;
        this.controlRangeSpecificationMapper = controlRangeSpecificationMapper;
        this.componentRepository = componentRepository;
    }

    @Transactional(readOnly = true)
    public List<ControlRangeSpecificationResponse> getByComponent(Long componentId) {
        List<ControlRangeSpecification> controlRangeSpecifications = controlRangeSpecificationRepository.findByComponent(componentId);
        return controlRangeSpecifications.stream()
                .map(controlRangeSpecificationMapper::toResponse)
                .toList();
    }

    public ControlRangeSpecificationResponse save(Long componentId, ControlRangeSpecificationCreateRequest request) {
        getEditableComponent(componentId);
        ControlRangeSpecification controlRangeSpecification = controlRangeSpecificationMapper.toDomain(componentId, request, componentRepository);
        ControlRangeSpecification saved = controlRangeSpecificationRepository.save(controlRangeSpecification);

        return controlRangeSpecificationMapper.toResponse(saved);
    }

    public ControlRangeSpecificationResponse update(Long id, ControlRangeSpecificationUpdateRequest request) {
        ControlRangeSpecification existing = findOrThrow(id);
        getEditableComponent(existing.componentId());
        ControlRangeSpecification updated = existing.update(
                request.method(),
                request.min(),
                request.max(),
                componentRepository
        );

        return controlRangeSpecificationMapper.toResponse(controlRangeSpecificationRepository.save(updated));
    }

    public void delete(Long id) {
        ControlRangeSpecification existing = findOrThrow(id);
        getEditableComponent(existing.componentId());

        controlRangeSpecificationRepository.save(existing.delete(componentRepository));
    }

    private ControlRangeSpecification findOrThrow(Long id) {
        return controlRangeSpecificationRepository.findById(id)
                .orElseThrow(() -> new ControlRangeSpecificationNotFoundException(id));
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
