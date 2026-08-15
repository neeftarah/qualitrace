package com.qualitrace.backend.application.mapper;

import com.qualitrace.backend.application.dto.SupplierCreateRequest;
import com.qualitrace.backend.application.dto.SupplierResponse;
import com.qualitrace.backend.domain.model.Supplier;
import org.springframework.stereotype.Service;

@Service
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.id(),
                supplier.code(),
                supplier.name(),
                supplier.address(),
                supplier.status()
        );
    }

    public Supplier toDomain(SupplierCreateRequest request) {
        return Supplier.createNew(
                request.code(),
                request.name(),
                request.address()
        );
    }
}
