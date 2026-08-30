package com.qualitrace.backend.component.domain.model;

import com.qualitrace.backend.component.domain.exception.ComponentRequiresSpecificationException;
import com.qualitrace.backend.specification.domain.repository.SpecificationRepository;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;

import java.time.Instant;

public record Component(Long id, ComponentType type, String reference, String name, Instant availableFrom, ComponentStatus status, Supplier supplier) {

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
                null,
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
                this.availableFrom,
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

    public Component setDraftIfActive() {
        if (this.status != ComponentStatus.ACTIVE) {
            return this;
        }

        return withStatus(ComponentStatus.DRAFT);
    }

    /**
     * Active un composant archivé ou en brouillon, le rendant disponible pour l'utilisation.
     *
     * RG-REF-05 : Le passage à l'état « Disponible » d'une matière première ou d’un produit fini est manuel et déclenche l'enregistrement de la date de mise à disposition.
     * Cette action n’est disponible que si la gamme de contrôle a été complétée.
     */
    public Component activate(SpecificationRepository specificationRepository) {
        if (this.status != ComponentStatus.ARCHIVED && this.status != ComponentStatus.DRAFT) {
            throw new IllegalStateException(
                    "Seul un composant archivé ou en brouillon peut être réactivé (statut actuel : %s)".formatted(this.status));
        }

        if (!specificationRepository.existsActiveSpecForComponent(id)) {
            throw new ComponentRequiresSpecificationException(id);
        }

        return new Component(
                this.id,
                this.type,
                this.reference,
                this.name,
                Instant.now(), // RG-REF-05
                ComponentStatus.ACTIVE,
                this.supplier
        );
    }

    private Component withStatus(ComponentStatus newStatus) {
        return new Component(this.id, this.type, this.reference, this.name, null, newStatus, this.supplier);
    }
}
