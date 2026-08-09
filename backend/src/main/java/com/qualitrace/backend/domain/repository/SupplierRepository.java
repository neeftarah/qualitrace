package com.qualitrace.backend.domain.repository;

import com.qualitrace.backend.domain.model.PageQuery;
import com.qualitrace.backend.domain.model.PageResult;
import com.qualitrace.backend.domain.model.Supplier;
import com.qualitrace.backend.domain.model.SupplierFilter;

import java.util.Optional;

public interface SupplierRepository {
    Optional<Supplier> findById(Long id);

    PageResult<Supplier> findAll(PageQuery pageQuery, SupplierFilter filter);

    Optional<Supplier> findByCode(String code);

    Optional<Supplier> findByName(String name);

    Supplier save(Supplier supplier);

    boolean existsById(Long id);
}
