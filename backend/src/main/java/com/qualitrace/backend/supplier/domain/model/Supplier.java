package com.qualitrace.backend.supplier.domain.model;

import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.supplier.domain.type.SupplierStatus;

public record Supplier(Long id, String code, String name, String address, SupplierStatus status) {

    public static Supplier createNew(String code, String name, String address) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier code cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier address cannot be null or empty");
        }

        return new Supplier(
                null, // Placeholder ID, will be replaced by the repository
                code,
                name,
                address,
                SupplierStatus.ACTIVE
        );
    }

    public Supplier update(String name, String address) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier address cannot be null or empty");
        }

        return new Supplier(
                this.id,
                this.code,
                name,
                address,
                this.status
        );
    }

    public Supplier archive(ComponentRepository componentRepository) {
        if (this.status == SupplierStatus.ARCHIVED) {
            throw new IllegalStateException("Le fournisseur est déjà archivé");
        }

        // RG-REF-02 : L'archivage d'un fournisseur entraîne automatiquement l'archivage de toutes les matières premières qui lui sont rattachées.
        componentRepository.archiveAllBySupplierId(this.id);

        return withStatus(SupplierStatus.ARCHIVED);
    }

    public Supplier reactivate() {
        if (this.status != SupplierStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Seul un fournisseur archivé peut être réactivé (statut actuel : %s)".formatted(this.status));
        }

        return withStatus(SupplierStatus.ACTIVE);
    }

    private Supplier withStatus(SupplierStatus newStatus) {
        return new Supplier(this.id, this.code, this.name, this.address, newStatus);
    }
}
