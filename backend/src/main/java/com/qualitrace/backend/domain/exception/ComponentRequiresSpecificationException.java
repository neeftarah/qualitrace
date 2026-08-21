package com.qualitrace.backend.domain.exception;

public class ComponentRequiresSpecificationException extends IllegalStateException {
    public ComponentRequiresSpecificationException(Long componentId) {
        super("Le composant %d doit avoir au moins une gamme de contrôle pour être activé".formatted(componentId));
    }
}