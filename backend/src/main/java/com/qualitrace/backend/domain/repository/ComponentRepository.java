package com.qualitrace.backend.domain.repository;

import com.qualitrace.backend.domain.model.Component;
import com.qualitrace.backend.domain.model.ComponentFilter;
import com.qualitrace.backend.domain.model.PageQuery;
import com.qualitrace.backend.domain.model.PageResult;

import java.util.Optional;

public interface ComponentRepository {
    Optional<Component> findById(Long id);

    PageResult<Component> findAll(PageQuery pageQuery, ComponentFilter filter);

    Optional<Component> findByReference(String reference);

    Optional<Component> findByName(String name);

    Component save(Component component);

    boolean existsById(Long id);
}
