package com.qualitrace.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_trail")
public class AuditTrailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "event", nullable = false)
    private String event;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "previous_data", nullable = false)
    private String previousData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changed_data", nullable = false)
    private String changedData;

    protected AuditTrailEntity() {
        // requis par JPA/Hibernate
    }

    public AuditTrailEntity(UUID authorId, String event, String entityType, String entityId,
                            Instant timestamp, String previousData, String changedData) {
        this.authorId = authorId;
        this.event = event;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = timestamp;
        this.previousData = previousData;
        this.changedData = changedData;
    }

    public Long getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getEvent() {
        return event;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPreviousData() {
        return previousData;
    }

    public String getChangedData() {
        return changedData;
    }
}