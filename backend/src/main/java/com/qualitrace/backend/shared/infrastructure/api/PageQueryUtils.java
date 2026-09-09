package com.qualitrace.backend.shared.infrastructure.api;

import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.SortQuery;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public final class PageQueryUtils {

    private PageQueryUtils() {
    }

    public static PageQuery toPageQuery(Pageable pageable, Set<String> allowedSortFields) {
        pageable.getSort().forEach(order -> {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new IllegalArgumentException("Champ de tri non autorisé : " + order.getProperty());
            }
        });

        List<SortQuery> sortOrders = pageable.getSort().stream()
                .map(order -> new SortQuery(
                        order.getProperty(),
                        order.isDescending() ? SortQuery.Direction.DESC : SortQuery.Direction.ASC
                ))
                .toList();

        return new PageQuery(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortOrders
        );
    }
}