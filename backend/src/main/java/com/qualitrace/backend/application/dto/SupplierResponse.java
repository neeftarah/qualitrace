package com.qualitrace.backend.application.dto;

import com.qualitrace.backend.domain.type.SupplierStatus;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "suppliers", itemRelation = "supplier")
public record SupplierResponse(
        Long id,
        String code,
        String name,
        String address,
        SupplierStatus status
) {
}
