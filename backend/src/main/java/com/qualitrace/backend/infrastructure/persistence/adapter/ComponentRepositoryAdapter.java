package com.qualitrace.backend.infrastructure.persistence.adapter;

import com.qualitrace.backend.domain.model.*;
import com.qualitrace.backend.domain.repository.ComponentRepository;
import com.qualitrace.backend.infrastructure.persistence.entity.ComponentEntity;
import com.qualitrace.backend.infrastructure.persistence.entity.SupplierEntity;
import com.qualitrace.backend.infrastructure.persistence.repository.ComponentJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ComponentRepositoryAdapter implements ComponentRepository {

    private final ComponentJpaRepository jpaRepository;

    public ComponentRepositoryAdapter(ComponentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Component> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Component> findByReference(String reference) {
        return jpaRepository.findByReference(reference).map(this::toDomain);
    }

    @Override
    public Optional<Component> findByName(String name) {
        return jpaRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public PageResult<Component> findAll(PageQuery pageQuery, ComponentFilter filter) {
        Sort sort = Sort.by(pageQuery.sort().stream()
                .map(s -> new Sort.Order(
                        s.direction() == SortQuery.Direction.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        s.field()))
                .toList());
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), sort);

        Page<ComponentEntity> page = jpaRepository.search(
                filter.type() != null ? filter.type().toString() : null,
                filter.reference(),
                filter.name(),
                filter.status() != null ? filter.status().name() : null,
                filter.supplierId() != null ? filter.supplierId() : null,
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
    public Component save(Component component) {
        ComponentEntity entity = component.id() != null
                ? jpaRepository.findById(component.id())
                .map(existing -> applyChanges(existing, component))
                .orElseGet(() -> toNewEntity(component))
                : toNewEntity(component);
        ComponentEntity saved = jpaRepository.saveAndFlush(entity);

        return toDomain(saved);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    /**
     * Construit une entité du domaine à partir d'une entité JPA
     *
     * @param entity Objet JPA
     * @return Objet du domaine (Component)
     */
    private Component toDomain(ComponentEntity entity) {
        return new Component(
                entity.getId(),
                entity.getType(),
                entity.getReference(),
                entity.getName(),
                entity.getStatus(),
                entity.getSupplier() != null ? new Supplier(
                        entity.getSupplier().getId(),
                        entity.getSupplier().getCode(),
                        entity.getSupplier().getName(),
                        entity.getSupplier().getAddress(),
                        entity.getSupplier().getStatus()
                ) : null
        );
    }

    private ComponentEntity applyChanges(ComponentEntity entity, Component component) {
        entity.setName(component.name());
        entity.setStatus(component.status());

        return entity;
    }

    private ComponentEntity toNewEntity(Component component) {
        return new ComponentEntity(
                component.id(),
                component.type(),
                component.reference(),
                component.name(),
                new SupplierEntity(
                        component.supplier().id(),
                        component.supplier().code(),
                        component.supplier().name(),
                        component.supplier().address(),
                        component.supplier().status()
                ),
                component.status()
        );
    }
}
