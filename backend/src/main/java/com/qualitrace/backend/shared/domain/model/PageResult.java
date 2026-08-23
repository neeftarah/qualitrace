package com.qualitrace.backend.shared.domain.model;

import java.util.List;

public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public <R> PageResult<R> map(java.util.function.Function<T, R> mapper) {
        return new PageResult<>(
                content.stream().map(mapper).toList(),
                page, size, totalElements, totalPages
        );
    }
}