package com.qualitrace.backend.controls.infrastructure.persistence.adapter;

import com.qualitrace.backend.controls.domain.model.ControlRangeSpecification;
import com.qualitrace.backend.controls.domain.repository.ControlRangeSpecificationRepository;
import com.qualitrace.backend.controls.domain.type.ControlRangeSpecificationStatus;
import com.qualitrace.backend.component.infrastructure.persistence.entity.ComponentEntity;
import com.qualitrace.backend.controls.infrastructure.persistence.entity.ControlRangeSpecificationEntity;
import com.qualitrace.backend.controls.infrastructure.persistence.repository.ControlRangeSpecificationJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ControlRangeSpecificationRepositoryAdapter implements ControlRangeSpecificationRepository {

    private final ControlRangeSpecificationJpaRepository jpaRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public ControlRangeSpecificationRepositoryAdapter(ControlRangeSpecificationJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ControlRangeSpecification> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ControlRangeSpecification> findByComponent(Long componentId) {
        return jpaRepository.findByComponentIdAndStatusNot(componentId, ControlRangeSpecificationStatus.DELETED)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsActiveSpecForComponent(Long componentId) {
        return jpaRepository.existsByComponentIdAndStatusNot(componentId, ControlRangeSpecificationStatus.DELETED);
    }

    @Override
    public ControlRangeSpecification save(ControlRangeSpecification controlRangeSpecification) {
        ControlRangeSpecificationEntity entity = controlRangeSpecification.id() != null
                ? jpaRepository.findById(controlRangeSpecification.id())
                .map(existing -> applyChanges(existing, controlRangeSpecification))
                .orElseGet(() -> toNewEntity(controlRangeSpecification))
                : toNewEntity(controlRangeSpecification);
        ControlRangeSpecificationEntity saved = jpaRepository.saveAndFlush(entity);

        return toDomain(saved);
    }

    /**
     * Construit une entité du domaine à partir d'une entité JPA
     *
     * @param entity Objet JPA
     * @return Objet du domaine (Component)
     */
    private ControlRangeSpecification toDomain(ControlRangeSpecificationEntity entity) {
        return new ControlRangeSpecification(
                entity.getId(),
                entity.getName(),
                entity.getMethod(),
                entity.getUnit(),
                entity.getMin(),
                entity.getMax(),
                entity.getStatus(),
                entity.getComponent().getId()
        );
    }

    private ControlRangeSpecificationEntity applyChanges(ControlRangeSpecificationEntity entity, ControlRangeSpecification controlRangeSpecification) {
        entity.setMethod(controlRangeSpecification.method());
        entity.setMin(controlRangeSpecification.min());
        entity.setMax(controlRangeSpecification.max());
        entity.setStatus(controlRangeSpecification.status());

        return entity;
    }

    private ControlRangeSpecificationEntity toNewEntity(ControlRangeSpecification controlRangeSpecification) {
        // entityManager.getReference() crée un proxy JPA léger rattaché au contexte de persistance
        ComponentEntity component = entityManager.getReference(
                ComponentEntity.class,
                controlRangeSpecification.componentId()
        );

        return new ControlRangeSpecificationEntity(
                controlRangeSpecification.id(),
                controlRangeSpecification.name(),
                controlRangeSpecification.method(),
                controlRangeSpecification.unit(),
                controlRangeSpecification.min(),
                controlRangeSpecification.max(),
                controlRangeSpecification.status(),
                component
        );
    }
}
