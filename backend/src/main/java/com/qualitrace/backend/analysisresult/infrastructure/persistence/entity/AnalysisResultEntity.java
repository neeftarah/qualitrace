package com.qualitrace.backend.analysisresult.infrastructure.persistence.entity;

import com.qualitrace.backend.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "analysis_results")
public class AnalysisResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "specification_id", nullable = false)
    private Long specificationId;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    protected AnalysisResultEntity() {
        // requis par JPA/Hibernate
    }

    public AnalysisResultEntity(
            Long id,
            Long batchId,
            Long specificationId,
            Double value,
            Instant createdAt,
            UserEntity createdBy
    ) {
        this.id = id;
        this.batchId = batchId;
        this.specificationId = specificationId;
        this.value = value;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getSpecificationId() {
        return specificationId;
    }

    public void setSpecificationId(Long specificationId) {
        this.specificationId = specificationId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }
}
