package com.qualitrace.backend.controls.domain.model;

import com.qualitrace.backend.controls.domain.type.ControlRangeSpecificationStatus;

public record ControlRangeSpecification(
        Long id,
        String name,
        String method,
        String unit,
        Double min,
        Double max,
        ControlRangeSpecificationStatus status,
        Long componentId
) {
    public static ControlRangeSpecification createNew(String name, String method, String unit, Double min, Double max, Long componentId) {
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

        return new ControlRangeSpecification(
                null, // Placeholder ID, will be replaced by the repository
                name,
                method,
                unit,
                min,
                max,
                ControlRangeSpecificationStatus.ACTIVE,
                componentId
        );
    }

    public ControlRangeSpecification update(String method, Double min, Double max) {
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

        return new ControlRangeSpecification(
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

    public ControlRangeSpecification delete() {
        if (this.status == ControlRangeSpecificationStatus.DELETED) {
            throw new IllegalStateException("Control range specification is already deleted");
        }

        return new ControlRangeSpecification(
                this.id,
                this.name,
                this.method,
                this.unit,
                this.min,
                this.max,
                ControlRangeSpecificationStatus.DELETED,
                this.componentId
        );
    }
}
