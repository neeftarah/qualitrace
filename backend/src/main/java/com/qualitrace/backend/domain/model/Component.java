package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.domain.type.ComponentStatus;
import com.qualitrace.backend.domain.type.ComponentType;

public record Component(Long id, ComponentType type, String reference, String name, ComponentStatus status, Supplier supplier) {

    public static Component createNew(ComponentType type, String reference, String name, Supplier supplier) {
        if (type == null) {
            throw new IllegalArgumentException("Component type cannot be null");
        }
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("Component reference cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be null or empty");
        }
        if (supplier == null) {
            throw new IllegalArgumentException("Component supplier cannot be null");
        }

        return new Component(
                null, // Placeholder ID, will be replaced by the repository
                type,
                reference,
                name,
                ComponentStatus.DRAFT, // RG-REF-03 - Toute matière première est créée avec l'état « Brouillon ».
                supplier
        );
    }

    public Component update(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Component name cannot be null or empty");
        }

        return new Component(
                this.id,
                this.type,
                this.reference,
                name,
                this.status,
                this.supplier
        );
    }

    public Component archive() {
        if (this.status == ComponentStatus.ARCHIVED) {
            throw new IllegalStateException("Le composant est déjà archivé");
        }

        return withStatus(ComponentStatus.ARCHIVED);
    }

    public Component draft() {
        if (this.status == ComponentStatus.DRAFT) {
            throw new IllegalStateException("Le composant est déjà en brouillon");
        }

        return withStatus(ComponentStatus.DRAFT);
    }

    public Component activate() {
        if (this.status != ComponentStatus.ARCHIVED && this.status != ComponentStatus.DRAFT) {
            throw new IllegalStateException(
                    "Seul un composant archivé ou en brouillon peut être réactivé (statut actuel : %s)".formatted(this.status));
        }

        return withStatus(ComponentStatus.ACTIVE);
    }

    private Component withStatus(ComponentStatus newStatus) {
        return new Component(this.id, this.type, this.reference, this.name, newStatus, this.supplier);
    }
}
