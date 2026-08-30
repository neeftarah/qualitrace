package com.qualitrace.backend.specification.infrastructure.persistence.adapter;

import com.qualitrace.backend.specification.domain.model.Specification;
import com.qualitrace.backend.specification.domain.repository.SpecificationRepository;
import com.qualitrace.backend.specification.domain.type.SpecificationStatus;
import com.qualitrace.backend.component.infrastructure.persistence.entity.ComponentEntity;
import com.qualitrace.backend.specification.infrastructure.persistence.entity.SpecificationEntity;
import com.qualitrace.backend.specification.infrastructure.persistence.repository.SpecificationJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SpecificationRepositoryAdapter implements SpecificationRepository {

    private final SpecificationJpaRepository jpaRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public SpecificationRepositoryAdapter(SpecificationJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Specification> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Specification> findByComponent(Long componentId) {
        return jpaRepository.findByComponentIdAndStatusNot(componentId, SpecificationStatus.DELETED)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsActiveSpecForComponent(Long componentId) {
        return jpaRepository.existsByComponentIdAndStatusNot(componentId, SpecificationStatus.DELETED);
    }

    @Override
    public Specification save(Specification specification) {
        SpecificationEntity entity = specification.id() != null
                ? jpaRepository.findById(specification.id())
                .map(existing -> applyChanges(existing, specification))
                .orElseGet(() -> toNewEntity(specification))
                : toNewEntity(specification);
        SpecificationEntity saved = jpaRepository.saveAndFlush(entity);

        return toDomain(saved);
    }

    /**
     * Construit une entité du domaine à partir d'une entité JPA
     *
     * @param entity Objet JPA
     * @return Objet du domaine (Component)
     */
    private Specification toDomain(SpecificationEntity entity) {
        return new Specification(
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

    private SpecificationEntity applyChanges(SpecificationEntity entity, Specification specification) {
        entity.setMethod(specification.method());
        entity.setMin(specification.min());
        entity.setMax(specification.max());
        entity.setStatus(specification.status());

        return entity;
    }

    private SpecificationEntity toNewEntity(Specification specification) {
        // entityManager.getReference() crée un proxy JPA léger rattaché au contexte de persistance
        ComponentEntity component = entityManager.getReference(
                ComponentEntity.class,
                specification.componentId()
        );

        return new SpecificationEntity(
                specification.id(),
                specification.name(),
                specification.method(),
                specification.unit(),
                specification.min(),
                specification.max(),
                specification.status(),
                component
        );
    }
}
