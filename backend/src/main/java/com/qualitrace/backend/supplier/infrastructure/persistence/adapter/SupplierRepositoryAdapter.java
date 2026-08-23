package com.qualitrace.backend.supplier.infrastructure.persistence.adapter;

import com.qualitrace.backend.supplier.domain.repository.SupplierRepository;
import com.qualitrace.backend.supplier.infrastructure.persistence.entity.SupplierEntity;
import com.qualitrace.backend.supplier.infrastructure.persistence.repository.SupplierJpaRepository;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.shared.domain.model.SortQuery;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.supplier.domain.model.SupplierFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SupplierRepositoryAdapter implements SupplierRepository {

    private final SupplierJpaRepository jpaRepository;

    public SupplierRepositoryAdapter(SupplierJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Supplier> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public Optional<Supplier> findByName(String name) {
        return jpaRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public PageResult<Supplier> findAll(PageQuery pageQuery, SupplierFilter filter) {
        Sort sort = Sort.by(pageQuery.sort().stream()
                .map(s -> new Sort.Order(
                        s.direction() == SortQuery.Direction.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
                        s.field()))
                .toList());
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), sort);

        Page<SupplierEntity> page = jpaRepository.search(
                filter.code(),
                filter.name(),
                filter.status() != null ? filter.status().name() : null,
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
    public Supplier save(Supplier supplier) {
        SupplierEntity entity = supplier.id() != null
                ? jpaRepository.findById(supplier.id())
                .map(existing -> applyChanges(existing, supplier))
                .orElseGet(() -> toNewEntity(supplier))
                : toNewEntity(supplier);
        SupplierEntity saved = jpaRepository.saveAndFlush(entity);

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
     * @return Objet du domaine (Supplier)
     */
    private Supplier toDomain(SupplierEntity entity) {
        return new Supplier(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAddress(),
                entity.getStatus()
        );
    }

    private SupplierEntity applyChanges(SupplierEntity entity, Supplier supplier) {
        entity.setCode(supplier.code());
        entity.setName(supplier.name());
        entity.setAddress(supplier.address());
        entity.setStatus(supplier.status());

        return entity;
    }

    private SupplierEntity toNewEntity(Supplier supplier) {
        return new SupplierEntity(
                supplier.id(),
                supplier.code(),
                supplier.name(),
                supplier.address(),
                supplier.status()
        );
    }
}
