package com.qualitrace.backend.supplier.domain.repository;

import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.supplier.domain.model.SupplierFilter;

import java.util.Optional;

public interface SupplierRepository {
    Optional<Supplier> findById(Long id);

    PageResult<Supplier> findAll(PageQuery pageQuery, SupplierFilter filter);

    Optional<Supplier> findByCode(String code);

    Optional<Supplier> findByName(String name);

    Supplier save(Supplier supplier);

    boolean existsById(Long id);
}
