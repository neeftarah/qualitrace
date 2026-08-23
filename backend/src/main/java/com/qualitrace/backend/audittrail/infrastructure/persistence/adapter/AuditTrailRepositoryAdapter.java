package com.qualitrace.backend.audittrail.infrastructure.persistence.adapter;

import com.qualitrace.backend.audittrail.domain.model.AuditTrail;
import com.qualitrace.backend.audittrail.domain.model.AuditTrailFilter;
import com.qualitrace.backend.audittrail.domain.repository.AuditTrailRepository;
import com.qualitrace.backend.audittrail.infrastructure.persistence.entity.AuditTrailEntity;
import com.qualitrace.backend.audittrail.infrastructure.persistence.repository.AuditTrailJpaRepository;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Repository
public class AuditTrailRepositoryAdapter implements AuditTrailRepository {

    private final AuditTrailJpaRepository jpaRepository;

    public AuditTrailRepositoryAdapter(AuditTrailJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PageResult<AuditTrail> findAll(PageQuery pageQuery, AuditTrailFilter filter) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());

        Instant from = filter.fromDate() != null
                ? filter.fromDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                : null;

        Instant to = filter.toDate() != null
                ? filter.toDate().atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant()
                : null;

        Page<AuditTrailEntity> page = jpaRepository.search(
                filter.author_id(),
                (filter.event() != null && !filter.event().isBlank()) ? filter.event().toUpperCase() : null,
                (filter.entity_type() != null && !filter.entity_type().isBlank()) ? filter.entity_type() : null,
                filter.entity_id(),
                (filter.content() != null && !filter.content().isBlank()) ? "%" + filter.content() + "%" : null,
                from,
                to,
                pageable
        );

        return new PageResult<>(
                page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Construit une entité du domaine à partir d'une entité JPA
     *
     * @param entity Objet JPA
     * @return Objet du domaine (AuditTrail)
     */
    private AuditTrail toDomain(AuditTrailEntity entity) {
        return new AuditTrail(
                entity.getId(),
                entity.getAuthor() != null ? new User(
                        entity.getAuthor().getId(),
                        entity.getAuthor().getLogin(),
                        entity.getAuthor().getPassword(),
                        entity.getAuthor().getEmail(),
                        entity.getAuthor().getFirstname(),
                        entity.getAuthor().getSurname(),
                        entity.getAuthor().getStatus(),
                        entity.getAuthor().getVersion(),
                        entity.getAuthor().getCreatedAt(),
                        entity.getAuthor().getUpdatedAt(),
                        entity.getAuthor().getRoles()
                ) : null,
                entity.getEvent(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getTimestamp(),
                entity.getPreviousData(),
                entity.getChangedData()
        );
    }
}
