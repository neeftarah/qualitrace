package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.component.domain.exception.ComponentRequiresSpecificationException;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.controls.domain.repository.ControlRangeSpecificationRepository;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentTest {
    @Mock
    private ControlRangeSpecificationRepository controlRepository;

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
        when(controlRepository.existsActiveSpecForComponent(any())).thenReturn(true);

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
        Component reactivatedComponent = archivedComponent.activate(controlRepository);
        assertThat(reactivatedComponent.status()).isEqualTo(ComponentStatus.ACTIVE);

        assertThatException().isThrownBy(() -> reactivatedComponent.activate(controlRepository)).isInstanceOf(IllegalStateException.class)
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
        Component reactivatedComponent2 = draftComponent.activate(controlRepository);
        assertThat(reactivatedComponent2.status()).isEqualTo(ComponentStatus.ACTIVE);
    }

    @Test
    void activateWithoutSpecificationShouldThrowException() {
        when(controlRepository.existsActiveSpecForComponent(any())).thenReturn(false);

        Component component = createComponent();
        assertThatException().isThrownBy(() -> component.activate(controlRepository))
                .isInstanceOf(ComponentRequiresSpecificationException.class);
    }

    @Test
    void setDraftIfActiveWhenActive() {
        when(controlRepository.existsActiveSpecForComponent(any())).thenReturn(true);

        Component component = createComponent().activate(controlRepository); // DRAFT -> ACTIVE
        Component result = component.setDraftIfActive();

        assertThat(result.status()).isEqualTo(ComponentStatus.DRAFT);
    }

    @Test
    void setDraftIfActiveWhenAlreadyDraft() {
        Component component = createComponent(); // déjà DRAFT
        Component result = component.setDraftIfActive();

        assertThat(result.status()).isEqualTo(ComponentStatus.DRAFT);
        assertThat(result).isEqualTo(component); // no-op : même instance/valeurs, pas de nouvel objet muté
    }

    @Test
    void setDraftIfActiveWhenArchived() {
        Component component = createComponent().archive(); // DRAFT -> ARCHIVED
        Component result = component.setDraftIfActive();

        assertThat(result.status()).isEqualTo(ComponentStatus.ARCHIVED); // inchangé, pas d'exception
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
