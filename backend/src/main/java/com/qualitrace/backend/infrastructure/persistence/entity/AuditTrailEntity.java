package com.qualitrace.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "audit_trail")
public class AuditTrailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;

    protected AuditTrailEntity() {
        // requis par JPA/Hibernate
    }

    public AuditTrailEntity(UserEntity author, String event, String entityType, String entityId,
                            Instant timestamp, String previousData, String changedData) {
        this.author = author;
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

    public UserEntity getAuthor() {
        return author;
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