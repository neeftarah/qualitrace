package com.qualitrace.backend.shared.domain.model;

import java.util.List;

/**
 * Agnostic pagination request parameters from the implementation.
 *
 * @param page The page index (0-based)
 * @param size The number of items per page
 * @param sort The sorting criteria
 */
public record PageQuery(int page, int size, List<SortQuery> sort) {
    public PageQuery {
        if (page < 0) throw new IllegalArgumentException("Page index must not be negative");
        if (size < 1) throw new IllegalArgumentException("Page size must be at least 1");
    }
}
