package com.qualitrace.backend.component.infrastructure.persistence.entity;

import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.supplier.infrastructure.persistence.entity.SupplierEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "components")
public class ComponentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ComponentType type;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ComponentStatus status;

    @Column(name = "available_from")
    private Instant availableFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;

    protected ComponentEntity() {
        // requis par JPA/Hibernate
    }

    public ComponentEntity(
            Long id,
            ComponentType type,
            String reference,
            String name,
            Instant availableFrom,
            SupplierEntity supplier,
            ComponentStatus status
    ) {
        this.id = id;
        this.type = type;
        this.reference = reference;
        this.name = name;
        this.availableFrom = availableFrom;
        this.status = status;
        this.supplier = supplier;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComponentType getType() {
        return type;
    }

    public void setType(ComponentType type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(Instant availableFrom) {
        this.availableFrom = availableFrom;
    }

    public SupplierEntity getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierEntity supplier) {
        this.supplier = supplier;
    }

    public ComponentStatus getStatus() {
        return status;
    }

    public void setStatus(ComponentStatus status) {
        this.status = status;
    }
}