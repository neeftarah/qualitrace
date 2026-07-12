package com.qualitrace.backend.domain.model;

public record SortQuery(String field, Direction direction) {
    public enum Direction { ASC, DESC }
}