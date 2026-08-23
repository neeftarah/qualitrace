package com.qualitrace.backend.supplier.application.service;

import com.qualitrace.backend.supplier.application.dto.SupplierCreateRequest;
import com.qualitrace.backend.supplier.application.dto.SupplierResponse;
import com.qualitrace.backend.supplier.application.dto.SupplierUpdateRequest;
import com.qualitrace.backend.supplier.application.mapper.SupplierMapper;
import com.qualitrace.backend.supplier.domain.exception.SupplierNotFoundException;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.supplier.domain.model.SupplierFilter;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.supplier.domain.repository.SupplierRepository;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final ComponentRepository componentRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            SupplierMapper supplierMapper,
            ComponentRepository componentRepository
    ) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.componentRepository = componentRepository;
    }

    public SupplierResponse getOneById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
        return supplierMapper.toResponse(supplier);
    }

    public PageResult<SupplierResponse> getAll(PageQuery pageQuery, SupplierFilter filter) {
        return supplierRepository.findAll(pageQuery, filter)
                .map(supplierMapper::toResponse);
    }

    public SupplierResponse save(SupplierCreateRequest request) {
        Supplier supplier = supplierMapper.toDomain(request);
        Supplier saved = supplierRepository.save(supplier);
        return supplierMapper.toResponse(saved);
    }

    public SupplierResponse update(Long id, SupplierUpdateRequest request) {
        Supplier existing = findOrThrow(id);
        Supplier updated = existing.update(
                request.name(),
                request.address()
        );

        return supplierMapper.toResponse(supplierRepository.save(updated));
    }

    @Transactional
    public SupplierResponse archive(Long id) {
        Supplier existing = findOrThrow(id);

        Supplier archived = supplierRepository.save(existing.archive());

        // RG-REF-02 : L'archivage d'un fournisseur entraîne automatiquement l'archivage de toutes les matières premières qui lui sont rattachées.
        componentRepository
                .findBySupplierIdAndStatusNot(id, ComponentStatus.ARCHIVED)
                .forEach(component -> componentRepository.save(component.archive()));

        return supplierMapper.toResponse(archived);
    }

    public SupplierResponse reactivate(Long id) {
        Supplier existing = findOrThrow(id);
        return supplierMapper.toResponse(supplierRepository.save(existing.reactivate()));
    }

    private Supplier findOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }
}