package com.qualitrace.backend.component.domain.exception;

public class ComponentDisabledException extends IllegalStateException {
    public ComponentDisabledException(Long componentId) {
        super("Le composant %d est désactivé : aucune gamme de contrôle ne peut y être modifiée".formatted(componentId));
    }
}
