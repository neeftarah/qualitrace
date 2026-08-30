package com.qualitrace.backend.specification.infrastructure.persistence.entity;

import com.qualitrace.backend.component.infrastructure.persistence.entity.ComponentEntity;
import com.qualitrace.backend.specification.domain.type.SpecificationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "control_range_specifications")
public class SpecificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "min", nullable = false)
    private Double min;

    @Column(name = "max", nullable = false)
    private Double max;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private SpecificationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private ComponentEntity component;

    protected SpecificationEntity() {
        // requis par JPA/Hibernate
    }

    public SpecificationEntity(Long id, String name, String method, String unit, Double min, Double max, SpecificationStatus status, ComponentEntity component) {
        this.id = id;
        this.name = name;
        this.method = method;
        this.unit = unit;
        this.min = min;
        this.max = max;
        this.status = status;
        this.component = component;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public SpecificationStatus getStatus() {
        return status;
    }

    public void setStatus(SpecificationStatus status) {
        this.status = status;
    }

    public ComponentEntity getComponent() {
        return component;
    }

    public void setComponent(ComponentEntity component) {
        this.component = component;
    }
}