package com.qualitrace.backend.deviation.infrastructure.persistence.entity;

import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "deviations")
public class DeviationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "code", nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviationStatus status;

    @Column(name = "comment", nullable = false)
    private String comment;

    protected DeviationEntity() {
        // requis par JPA/Hibernate
    }

    public DeviationEntity(
            Long id,
            Long batchId,
            String code,
            DeviationStatus status,
            String comment
    ) {
        this.id = id;
        this.batchId = batchId;
        this.code = code;
        this.status = status;
        this.comment = comment;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DeviationStatus getStatus() {
        return status;
    }

    public void setStatus(DeviationStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
