package com.qualitrace.backend.shared.domain.model;

public record SortQuery(String field, Direction direction) {
    public enum Direction { ASC, DESC }
}