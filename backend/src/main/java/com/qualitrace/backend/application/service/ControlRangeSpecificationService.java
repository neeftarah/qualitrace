package com.qualitrace.backend.application.service;

import com.qualitrace.backend.application.dto.ControlRangeSpecificationCreateRequest;
import com.qualitrace.backend.application.dto.ControlRangeSpecificationResponse;
import com.qualitrace.backend.application.dto.ControlRangeSpecificationUpdateRequest;
import com.qualitrace.backend.application.mapper.ControlRangeSpecificationMapper;
import com.qualitrace.backend.domain.exception.ComponentDisabledException;
import com.qualitrace.backend.domain.exception.ComponentNotFoundException;
import com.qualitrace.backend.domain.exception.ControlRangeSpecificationNotFoundException;
import com.qualitrace.backend.domain.model.Component;
import com.qualitrace.backend.domain.model.ControlRangeSpecification;
import com.qualitrace.backend.domain.repository.ComponentRepository;
import com.qualitrace.backend.domain.repository.ControlRangeSpecificationRepository;
import com.qualitrace.backend.domain.type.ComponentStatus;
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
        Component component = getEditableComponent(componentId);
        ControlRangeSpecification controlRangeSpecification = controlRangeSpecificationMapper.toDomain(componentId, request);
        ControlRangeSpecification saved = controlRangeSpecificationRepository.save(controlRangeSpecification);
        setComponentToDraft(component);

        return controlRangeSpecificationMapper.toResponse(saved);
    }

    public ControlRangeSpecificationResponse update(Long componentId, Long id, ControlRangeSpecificationUpdateRequest request) {
        Component component = getEditableComponent(componentId);
        ControlRangeSpecification existing = findOrThrow(id);
        ControlRangeSpecification updated = existing.update(
                request.method(),
                request.min(),
                request.max()
        );
        setComponentToDraft(component);

        return controlRangeSpecificationMapper.toResponse(controlRangeSpecificationRepository.save(updated));
    }

    public ControlRangeSpecificationResponse delete(Long componentId, Long id) {
        Component component = getEditableComponent(componentId);
        ControlRangeSpecification existing = findOrThrow(id);
        setComponentToDraft(component);

        return controlRangeSpecificationMapper.toResponse(controlRangeSpecificationRepository.save(existing.delete()));
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

    private void setComponentToDraft(Component component) {
        if (component.status() == ComponentStatus.ACTIVE) {
            componentRepository.save(component.setDraftIfActive());
        }
    }
}