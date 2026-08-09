package com.qualitrace.backend.application.service;

import com.qualitrace.backend.application.dto.SupplierCreateRequest;
import com.qualitrace.backend.application.dto.SupplierResponse;
import com.qualitrace.backend.application.dto.SupplierUpdateRequest;
import com.qualitrace.backend.application.mapper.SupplierMapper;
import com.qualitrace.backend.domain.exception.SupplierNotFoundException;
import com.qualitrace.backend.domain.model.PageQuery;
import com.qualitrace.backend.domain.model.PageResult;
import com.qualitrace.backend.domain.model.Supplier;
import com.qualitrace.backend.domain.model.SupplierFilter;
import com.qualitrace.backend.domain.repository.SupplierRepository;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
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

    public SupplierResponse archive(Long id) {
        Supplier existing = findOrThrow(id);
        return supplierMapper.toResponse(supplierRepository.save(existing.archive()));
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