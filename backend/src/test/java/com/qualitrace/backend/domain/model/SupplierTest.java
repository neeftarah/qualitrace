package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.domain.type.SupplierStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;


class SupplierTest {
    @Test
    void createNewSupplier() {
        Supplier supplier = createSupplier();

        assertThat(supplier.code()).isEqualTo("SUP001");
        assertThat(supplier.name()).isEqualTo("Supplier 1");
        assertThat(supplier.address()).isEqualTo("123 Main St, City, Country");
        assertThat(supplier.status()).isEqualTo(SupplierStatus.ACTIVE);
    }

    @Test
    void createNewSupplierWithoutCode() {
        assertThatException().isThrownBy(() -> Supplier.createNew(
                        null,
                        "Supplier 1",
                        "123 Main St, City, Country"
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier code cannot be null or empty");

        assertThatException().isThrownBy(() -> Supplier.createNew(
                        "",
                        "Supplier 1",
                        "123 Main St, City, Country"
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier code cannot be null or empty");
    }

    @Test
    void createNewSupplierWithoutName() {
        assertThatException().isThrownBy(() -> Supplier.createNew(
                        "SUP001",
                        null,
                        "123 Main St, City, Country"
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier name cannot be null or empty");

        assertThatException().isThrownBy(() -> Supplier.createNew(
                        "SUP001",
                        "",
                        "123 Main St, City, Country"
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier name cannot be null or empty");
    }

    @Test
    void createNewSupplierWithoutAddress() {
        assertThatException().isThrownBy(() -> Supplier.createNew(
                        "SUP001",
                        "Supplier 1",
                        null
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier address cannot be null or empty");

        assertThatException().isThrownBy(() -> Supplier.createNew(
                        "SUP001",
                        "Supplier 1",
                        ""
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier address cannot be null or empty");
    }

    @Test
    void updateSupplier() {
        Supplier supplier = createSupplier();
        Supplier supplierUpdated = supplier.update(
                "Supplier 1 Updated",
                "456 Elm St, City, Country"
        );

        assertThat(supplierUpdated.code()).isEqualTo("SUP001");
        assertThat(supplierUpdated.name()).isEqualTo("Supplier 1 Updated");
        assertThat(supplierUpdated.address()).isEqualTo("456 Elm St, City, Country");
        assertThat(supplierUpdated.status()).isEqualTo(SupplierStatus.ACTIVE);
    }

    @Test
    void updateSupplierWithoutName() {
        Supplier supplier = createSupplier();

        assertThatException().isThrownBy(() -> supplier.update(
                        null,
                        "123 Main St, City, Country"
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier name cannot be null or empty");

        assertThatException().isThrownBy(() -> supplier.update(
                        "",
                        "123 Main St, City, Country"
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier name cannot be null or empty");
    }

    @Test
    void updateSupplierWithoutAddress() {
        Supplier supplier = createSupplier();

        assertThatException().isThrownBy(() -> supplier.update(
                        "SUPP02",
                        null
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier address cannot be null or empty");

        assertThatException().isThrownBy(() -> supplier.update(
                        "SUPP02",
                        ""
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier address cannot be null or empty");
    }

    @Test
    void changeStatuses() {
        Supplier supplier = createSupplier();
        assertThat(supplier.status()).isEqualTo(SupplierStatus.ACTIVE);

        assertThatException().isThrownBy(supplier::reactivate).isInstanceOf(IllegalStateException.class)
                .withMessage("Seul un fournisseur archivé peut être réactivé (statut actuel : ACTIVE)");

        // ACTIVE ==> ARCHIVED
        Supplier archivedSupplier = supplier.archive();
        assertThat(archivedSupplier.status()).isEqualTo(SupplierStatus.ARCHIVED);

        assertThatException().isThrownBy(archivedSupplier::archive).isInstanceOf(IllegalStateException.class)
                .withMessage("Le fournisseur est déjà archivé");

        // ARCHIVED ==> ACTIVE
        Supplier reactivatedSupplier = archivedSupplier.reactivate();
        assertThat(reactivatedSupplier.status()).isEqualTo(SupplierStatus.ACTIVE);
    }

    private Supplier createSupplier() {
        return Supplier.createNew(
                "SUP001",
                "Supplier 1",
                "123 Main St, City, Country"
        );
    }
}