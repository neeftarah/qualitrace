package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.specification.domain.model.Specification;
import com.qualitrace.backend.specification.domain.type.SpecificationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpecificationTest {
    @Mock
    private ComponentRepository componentRepository;

    @Test
    void createNewSpecification() {
        Specification spec = createSpec();

        assertThat(spec.name()).isEqualTo("PH");
        assertThat(spec.method()).isEqualTo("pH-3215");
        assertThat(spec.unit()).isEqualTo("pH");
        assertThat(spec.min()).isEqualTo(6.5);
        assertThat(spec.max()).isEqualTo(7.5);
        assertThat(spec.status()).isEqualTo(SpecificationStatus.ACTIVE);
        assertThat(spec.componentId()).isEqualTo(1L);
        verify(componentRepository).setToDraft(1L);
    }

    @Test
    void createNewSpecificationWithoutName() {
        assertThatException().isThrownBy(() -> Specification.createNew(
                        null, "pH-3215", "pH", 6.5, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification name cannot be null or empty");

        assertThatException().isThrownBy(() -> Specification.createNew(
                        "", "pH-3215", "pH", 6.5, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification name cannot be null or empty");
    }

    @Test
    void createNewSpecificationWithoutMethod() {
        assertThatException().isThrownBy(() -> Specification.createNew(
                        "PH", null, "pH", 6.5, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification method cannot be null or empty");
    }

    @Test
    void createNewSpecificationWithoutUnit() {
        assertThatException().isThrownBy(() -> Specification.createNew(
                        "PH", "pH-3215", null, 6.5, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification unit cannot be null or empty");
    }

    @Test
    void createNewSpecificationWithoutComponentId() {
        assertThatException().isThrownBy(() -> Specification.createNew(
                        "PH", "pH-3215", "pH", 6.5, 7.5, null, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification component ID cannot be null");
    }

    @Test
    void createNewSpecificationWithInvalidRange() {
        assertThatException().isThrownBy(() -> Specification.createNew(
                        "PH", "pH-3215", "pH", 8.0, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification minimum must be less than maximum");
    }

    @Test
    void updateSpecification() {
        Specification spec = createSpec();
        Mockito.clearInvocations(componentRepository);

        Specification updated = spec.update("pH-7843", 6.0, 8.0, componentRepository);

        assertThat(updated.name()).isEqualTo("PH");
        assertThat(updated.method()).isEqualTo("pH-7843");
        assertThat(updated.min()).isEqualTo(6.0);
        assertThat(updated.max()).isEqualTo(8.0);
        assertThat(updated.componentId()).isEqualTo(1L); // inchangé
        assertThat(updated.status()).isEqualTo(SpecificationStatus.ACTIVE); // inchangé
        verify(componentRepository).setToDraft(1L);
    }

    @Test
    void deleteSpecification() {
        Specification spec = createSpec();
        Mockito.clearInvocations(componentRepository);

        Specification deleted = spec.delete(componentRepository);

        assertThat(deleted.status()).isEqualTo(SpecificationStatus.DELETED);
        verify(componentRepository).setToDraft(1L);
    }

    @Test
    void deleteAlreadyDeletedSpecificationShouldFail() {
        Specification deleted = createSpec().delete(componentRepository);

        assertThatException().isThrownBy(() -> deleted.delete(componentRepository))
                .isInstanceOf(IllegalStateException.class)
                .withMessage("Control range specification is already deleted");
    }

    private Specification createSpec() {
        return Specification.createNew("PH", "pH-3215", "pH", 6.5, 7.5, 1L, componentRepository);
    }
}
