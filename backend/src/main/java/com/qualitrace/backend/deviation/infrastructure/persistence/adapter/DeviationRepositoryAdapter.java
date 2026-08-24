package com.qualitrace.backend.deviation.infrastructure.persistence.adapter;

import com.qualitrace.backend.deviation.domain.model.Deviation;
import com.qualitrace.backend.deviation.domain.repository.DeviationRepository;
import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import com.qualitrace.backend.deviation.infrastructure.persistence.entity.DeviationEntity;
import com.qualitrace.backend.deviation.infrastructure.persistence.repository.DeviationJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DeviationRepositoryAdapter implements DeviationRepository {

    private final DeviationJpaRepository jpaRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public DeviationRepositoryAdapter(DeviationJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Deviation> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Deviation save(Deviation result) {
        DeviationEntity entity = result.id() != null
                ? jpaRepository.findById(result.id())
                .map(existing -> applyChanges(existing, result))
                .orElseGet(() -> toNewEntity(result))
                : toNewEntity(result);
        DeviationEntity saved = jpaRepository.saveAndFlush(entity);

        return toDomain(saved);
    }

    @Override
    public boolean existsByBatchIdAndStatus(Long id, DeviationStatus deviationStatus) {
        return jpaRepository.existsByBatchIdAndStatus(id, deviationStatus);
    }

    @Override
    public List<Deviation> findAllByBatchId(Long id) {
        return jpaRepository.findAllByBatchId(id)
                .stream().map(this::toDomain).toList();
    }

    private DeviationEntity applyChanges(DeviationEntity entity, Deviation deviation) {
        entity.setCode(deviation.code());
        entity.setStatus(deviation.status());
        entity.setComment(deviation.comment());

        return entity;
    }

    /**
     * Construit une entité du domaine à partir d'une entité JPA
     *
     * @param entity Objet JPA
     * @return Objet du domaine (Deviation)
     */
    private Deviation toDomain(DeviationEntity entity) {
        return new Deviation(
                entity.getId(),
                entity.getBatchId(),
                entity.getCode(),
                entity.getStatus(),
                entity.getComment()
        );
    }

    private DeviationEntity toNewEntity(Deviation deviation) {
        return new DeviationEntity(
                deviation.id(),
                deviation.batchId(),
                deviation.code(),
                deviation.status(),
                deviation.comment()
        );
    }
}
