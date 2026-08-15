package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.domain.type.ComponentStatus;
import com.qualitrace.backend.domain.type.ComponentType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;


class ComponentTest {
    @Test
    void createNewComponent() {
        Component component = createComponent();

        assertThat(component.type()).isEqualTo(ComponentType.RAW_MATERIAL);
        assertThat(component.reference()).isEqualTo("COMP-001");
        assertThat(component.name()).isEqualTo("Component 1");
        assertThat(component.supplier().code()).isEqualTo("SUP001");
        assertThat(component.supplier().name()).isEqualTo("Supplier 1");
        assertThat(component.supplier().address()).isEqualTo("123 Main St, City, Country");
        assertThat(component.status()).isEqualTo(ComponentStatus.DRAFT);
    }

    @Test
    void createNewComponentWithoutType() {
        assertThatException().isThrownBy(() -> Component.createNew(
                        null,
                        "COMP-001",
                        "Component 1",
                        Supplier.createNew("SUP001", "Supplier 1", "123 Main St, City, Country")
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component type cannot be null");
    }

    @Test
    void createNewComponentWithoutReference() {
        assertThatException().isThrownBy(() -> Component.createNew(
                        ComponentType.RAW_MATERIAL,
                        null,
                        "Component 1",
                        Supplier.createNew("SUP001", "Supplier 1", "123 Main St, City, Country")
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component reference cannot be null or empty");

        assertThatException().isThrownBy(() -> Component.createNew(
                        ComponentType.RAW_MATERIAL,
                        "",
                        "Component 1",
                        Supplier.createNew("SUP001", "Supplier 1", "123 Main St, City, Country")
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component reference cannot be null or empty");
    }

    @Test
    void createNewComponentWithoutName() {
        assertThatException().isThrownBy(() -> Component.createNew(
                        ComponentType.RAW_MATERIAL,
                        "COMP-001",
                        null,
                        Supplier.createNew("SUP001", "Supplier 1", "123 Main St, City, Country")
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component name cannot be null or empty");

        assertThatException().isThrownBy(() -> Component.createNew(
                        ComponentType.RAW_MATERIAL,
                        "COMP-001",
                        "",
                        Supplier.createNew("SUP001", "Supplier 1", "123 Main St, City, Country")
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component name cannot be null or empty");
    }

    @Test
    void createNewComponentWithoutSupplier() {
        assertThatException().isThrownBy(() -> Component.createNew(
                        ComponentType.RAW_MATERIAL,
                        "COMP-001",
                        "Component 1",
                        null
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component supplier cannot be null");
    }

    @Test
    void updateComponent() {
        Component component = createComponent();
        Component componentUpdated = component.update(
                "Component 1 Updated"
        );

        assertThat(component.type()).isEqualTo(ComponentType.RAW_MATERIAL);
        assertThat(componentUpdated.reference()).isEqualTo("COMP-001");
        assertThat(componentUpdated.name()).isEqualTo("Component 1 Updated");
        assertThat(component.supplier().code()).isEqualTo("SUP001");
        assertThat(component.supplier().name()).isEqualTo("Supplier 1");
        assertThat(component.supplier().address()).isEqualTo("123 Main St, City, Country");
        assertThat(componentUpdated.status()).isEqualTo(ComponentStatus.DRAFT);
    }

    @Test
    void updateComponentWithoutName() {
        Component component = createComponent();

        assertThatException().isThrownBy(() -> component.update(
                        null
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component name cannot be null or empty");

        assertThatException().isThrownBy(() -> component.update(
                        ""
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component name cannot be null or empty");
    }

    @Test
    void changeStatuses() {
        Component component = createComponent();
        assertThat(component.status()).isEqualTo(ComponentStatus.DRAFT);

        assertThatException().isThrownBy(component::draft).isInstanceOf(IllegalStateException.class)
                .withMessage("Le composant est déjà en brouillon");

        // ACTIVE ==> ARCHIVED
        Component archivedComponent = component.archive();
        assertThat(archivedComponent.status()).isEqualTo(ComponentStatus.ARCHIVED);

        assertThatException().isThrownBy(archivedComponent::archive).isInstanceOf(IllegalStateException.class)
                .withMessage("Le composant est déjà archivé");

        // ARCHIVED ==> ACTIVE
        Component reactivatedComponent = archivedComponent.activate();
        assertThat(reactivatedComponent.status()).isEqualTo(ComponentStatus.ACTIVE);

        assertThatException().isThrownBy(reactivatedComponent::activate).isInstanceOf(IllegalStateException.class)
                .withMessage("Seul un composant archivé ou en brouillon peut être réactivé (statut actuel : ACTIVE)");

        // ACTIVE ==> DRAFT
        Component draftComponent = reactivatedComponent.draft();
        assertThat(draftComponent.status()).isEqualTo(ComponentStatus.DRAFT);

        assertThatException().isThrownBy(draftComponent::draft).isInstanceOf(IllegalStateException.class)
                .withMessage("Le composant est déjà en brouillon");

        // ARCHIVED ==> DRAFT
        Component draftComponent2 = archivedComponent.draft();
        assertThat(draftComponent2.status()).isEqualTo(ComponentStatus.DRAFT);

        assertThatException().isThrownBy(draftComponent2::draft).isInstanceOf(IllegalStateException.class)
                .withMessage("Le composant est déjà en brouillon");

        // DRAFT ==> ACTIVE
        Component reactivatedComponent2 = draftComponent.activate();
        assertThat(reactivatedComponent2.status()).isEqualTo(ComponentStatus.ACTIVE);
    }

    private Component createComponent() {
        return Component.createNew(
                ComponentType.RAW_MATERIAL,
                "COMP-001",
                "Component 1",
                Supplier.createNew("SUP001", "Supplier 1", "123 Main St, City, Country")
        );
    }
}