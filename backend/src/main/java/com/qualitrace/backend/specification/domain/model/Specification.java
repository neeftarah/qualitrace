package com.qualitrace.backend.specification.domain.model;

import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.specification.domain.type.SpecificationStatus;

public record Specification(
        Long id,
        String name,
        String method,
        String unit,
        Double min,
        Double max,
        SpecificationStatus status,
        Long componentId
) {
    public static Specification createNew(
            String name,
            String method,
            String unit,
            Double min,
            Double max,
            Long componentId,
            ComponentRepository componentRepository
    ) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Control range specification name cannot be null or empty");
        }

        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Control range specification method cannot be null or empty");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Control range specification unit cannot be null or empty");
        }
        if (min == null) {
            throw new IllegalArgumentException("Control range specification minimum cannot be null");
        }
        if (max == null) {
            throw new IllegalArgumentException("Control range specification maximum cannot be null");
        }
        if (min >= max) {
            throw new IllegalArgumentException("Control range specification minimum must be less than maximum");
        }
        if (componentId == null) {
            throw new IllegalArgumentException("Control range specification component ID cannot be null");
        }

        componentRepository.setToDraft(componentId);

        return new Specification(
                null, // Placeholder ID, will be replaced by the repository
                name,
                method,
                unit,
                min,
                max,
                SpecificationStatus.ACTIVE,
                componentId
        );
    }

    public Specification update(String method, Double min, Double max, ComponentRepository componentRepository) {
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Control range specification method cannot be null or empty");
        }
        if (min == null) {
            throw new IllegalArgumentException("Control range specification minimum cannot be null");
        }
        if (max == null) {
            throw new IllegalArgumentException("Control range specification maximum cannot be null");
        }
        if (min >= max) {
            throw new IllegalArgumentException("Control range specification minimum must be less than maximum");
        }

        componentRepository.setToDraft(this.componentId);

        return new Specification(
                this.id,
                this.name,
                method,
                this.unit,
                min,
                max,
                this.status,
                this.componentId
        );
    }

    public Specification delete(ComponentRepository componentRepository) {
        if (this.status == SpecificationStatus.DELETED) {
            throw new IllegalStateException("Control range specification is already deleted");
        }

        componentRepository.setToDraft(this.componentId);

        return new Specification(
                this.id,
                this.name,
                this.method,
                this.unit,
                this.min,
                this.max,
                SpecificationStatus.DELETED,
                this.componentId
        );
    }
}
