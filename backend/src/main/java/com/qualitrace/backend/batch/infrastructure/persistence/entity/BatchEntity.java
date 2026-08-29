package com.qualitrace.backend.batch.infrastructure.persistence.entity;

import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.component.infrastructure.persistence.entity.ComponentEntity;
import com.qualitrace.backend.supplier.infrastructure.persistence.entity.SupplierEntity;
import com.qualitrace.backend.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "batches")
public class BatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private ComponentEntity component;

    @Column(name = "internal_reference_number", nullable = false, unique = true)
    private String internalReferenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;

    @Column(name = "supplier_reference_number", nullable = false, length = 15)
    private String supplierReferenceNumber;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "reception_date", nullable = false)
    private Instant receptionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private BatchStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by")
    private UserEntity validatedBy;

    @Column(name = "validated_at")
    private Instant validatedAt;

    // verrouillage optimiste
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // Lié à @version + entité immutable
    @Transient
    private boolean isNew;

    protected BatchEntity() {
        // requis par JPA/Hibernate
    }

    public BatchEntity(
            Long id,
            ComponentEntity component,
            SupplierEntity supplier,
            String internalReferenceNumber,
            String supplierReferenceNumber,
            Instant expiryDate,
            Instant receptionDate,
            BatchStatus status,
            boolean isNew
    ) {
        this.id = id;
        this.component = component;
        this.supplier = supplier;
        this.internalReferenceNumber = internalReferenceNumber;
        this.supplierReferenceNumber = supplierReferenceNumber;
        this.expiryDate = expiryDate;
        this.receptionDate = receptionDate;
        this.status = status;
        this.isNew = isNew;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComponentEntity getComponent() {
        return component;
    }

    public void setComponent(ComponentEntity component) {
        this.component = component;
    }

    public String getInternalReferenceNumber() {
        return internalReferenceNumber;
    }

    public void setInternalReferenceNumber(String internalReferenceNumber) {
        this.internalReferenceNumber = internalReferenceNumber;
    }

    public SupplierEntity getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierEntity supplier) {
        this.supplier = supplier;
    }

    public String getSupplierReferenceNumber() {
        return supplierReferenceNumber;
    }

    public void setSupplierReferenceNumber(String supplierReferenceNumber) {
        this.supplierReferenceNumber = supplierReferenceNumber;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Instant getReceptionDate() {
        return receptionDate;
    }

    public void setReceptionDate(Instant receptionDate) {
        this.receptionDate = receptionDate;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public UserEntity getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(UserEntity validatedBy) {
        this.validatedBy = validatedBy;
    }

    public Instant getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(Instant validatedAt) {
        this.validatedAt = validatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean isNew() {
        return isNew;
    }
}
