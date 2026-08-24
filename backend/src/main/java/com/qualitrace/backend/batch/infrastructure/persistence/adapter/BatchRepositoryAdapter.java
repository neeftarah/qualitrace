package com.qualitrace.backend.batch.infrastructure.persistence.adapter;

import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.model.BatchFilter;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.batch.infrastructure.persistence.entity.BatchEntity;
import com.qualitrace.backend.batch.infrastructure.persistence.repository.BatchJpaRepository;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.component.infrastructure.persistence.entity.ComponentEntity;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.shared.domain.model.SortQuery;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.supplier.infrastructure.persistence.entity.SupplierEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Repository
public class BatchRepositoryAdapter implements BatchRepository {

    private final BatchJpaRepository jpaRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public BatchRepositoryAdapter(BatchJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Batch> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<Batch> findAll(PageQuery pageQuery, BatchFilter filter) {
        Sort sort = Sort.by(pageQuery.sort().stream()
                .map(s -> new Sort.Order(
                        s.direction() == SortQuery.Direction.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        s.field()))
                .toList());
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), sort);

        String internalRefFilter = filter.internalReferenceNumber() != null && !filter.internalReferenceNumber().isBlank() ? "%" + filter.internalReferenceNumber().toLowerCase() + "%" : null;
        String supplierRefFilter = filter.supplierReferenceNumber() != null && !filter.supplierReferenceNumber().isBlank() ? "%" + filter.supplierReferenceNumber().toLowerCase() + "%" : null;

        Instant validationFrom = filter.validationFromDate() != null
                ? filter.validationFromDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                : null;

        Instant validationTo = filter.validationToDate() != null
                ? filter.validationToDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                : null;

        Instant expiryFrom = filter.expiryFromDate() != null
                ? filter.expiryFromDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                : null;

        Instant expiryTo = filter.expiryToDate() != null
                ? filter.expiryToDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                : null;

        Instant receptionFrom = filter.receptionFromDate() != null
                ? filter.receptionFromDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                : null;

        Instant receptionTo = filter.receptionToDate() != null
                ? filter.receptionToDate().atStartOfDay(ZoneOffset.UTC).toInstant()
                : null;

        Page<BatchEntity> page = jpaRepository.search(
                internalRefFilter,
                filter.supplierId(),
                supplierRefFilter,
                expiryFrom,
                expiryTo,
                receptionFrom,
                receptionTo,
                filter.status(),
                filter.validatedBy(),
                validationFrom,
                validationTo,
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

    @Override
    public String nextInternalReferenceNumber(ComponentType type, Instant receptionDate) {
        String typeCode = switch (type) {
            case RAW_MATERIAL -> "MP";
            case COMPONENT -> "PC";
        };

        LocalDate date = receptionDate.atZone(ZoneOffset.UTC).toLocalDate();
        String yearMonth = DateTimeFormatter.ofPattern("yyyyMM").format(date);
        String prefix = "LOT-%s-%s-".formatted(typeCode, yearMonth);

        return jpaRepository.findMaxInternalReferenceByPrefix(prefix)
                .map(maxRef -> {
                    // Extrait le numéro ZZZ final et incrémente
                    String seqStr = maxRef.substring(prefix.length());
                    int nextSeq = Integer.parseInt(seqStr) + 1;
                    return "%s%03d".formatted(prefix, nextSeq);
                })
                .orElseGet(() -> prefix + "001");
    }

    @Override
    public Batch save(Batch batch) {
        BatchEntity entity = batch.id() != null
                ? jpaRepository.findById(batch.id())
                .map(existing -> applyChanges(existing, batch))
                .orElseGet(() -> toNewEntity(batch))
                : toNewEntity(batch);
        BatchEntity saved = jpaRepository.saveAndFlush(entity);

        return toDomain(saved);
    }

    /**
     * Construit une entité du domaine à partir d'une entité JPA
     *
     * @param entity Objet JPA
     * @return Objet du domaine (Batch)
     */
    private Batch toDomain(BatchEntity entity) {
        return new Batch(
                entity.getId(),
                entity.getComponent() != null ? new Component(
                        entity.getComponent().getId(),
                        entity.getComponent().getType(),
                        entity.getComponent().getReference(),
                        entity.getComponent().getName(),
                        entity.getComponent().getAvailableFrom(),
                        entity.getComponent().getStatus(),
                        entity.getComponent().getSupplier() != null ? new Supplier(
                                entity.getComponent().getSupplier().getId(),
                                entity.getComponent().getSupplier().getCode(),
                                entity.getComponent().getSupplier().getName(),
                                entity.getComponent().getSupplier().getAddress(),
                                entity.getComponent().getSupplier().getStatus()
                        ) : null
                ) : null,
                entity.getInternalReferenceNumber(),
                entity.getSupplierReferenceNumber(),
                entity.getExpiryDate(),
                entity.getReceptionDate(),
                entity.getStatus()
        );
    }

    private BatchEntity applyChanges(BatchEntity entity, Batch batch) {
        entity.setStatus(batch.status());

        return entity;
    }

    private BatchEntity toNewEntity(Batch batch) {
        SupplierEntity supplierRef = batch.supplier() != null && batch.supplier().id() != null
                ? entityManager.getReference(SupplierEntity.class, batch.supplier().id())
                : null;
        ComponentEntity componentRef = batch.component() != null && batch.component().id() != null
                ? entityManager.getReference(ComponentEntity.class, batch.component().id())
                : null;

        return new BatchEntity(
                batch.id(),
                componentRef,
                supplierRef,
                batch.internalReferenceNumber(),
                batch.supplierReferenceNumber(),
                batch.expiryDate(),
                batch.receptionDate(),
                batch.status(),
                Instant.now(),
                null,
                true
        );
    }
}
