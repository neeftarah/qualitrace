package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.controls.domain.model.ControlRangeSpecification;
import com.qualitrace.backend.controls.domain.type.ControlRangeSpecificationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ControlRangeSpecificationTest {
    @Mock
    private ComponentRepository componentRepository;

    @Test
    void createNewControlRangeSpecification() {
        ControlRangeSpecification spec = createSpec();

        assertThat(spec.name()).isEqualTo("PH");
        assertThat(spec.method()).isEqualTo("pH-3215");
        assertThat(spec.unit()).isEqualTo("pH");
        assertThat(spec.min()).isEqualTo(6.5);
        assertThat(spec.max()).isEqualTo(7.5);
        assertThat(spec.status()).isEqualTo(ControlRangeSpecificationStatus.ACTIVE);
        assertThat(spec.componentId()).isEqualTo(1L);
        verify(componentRepository).setToDraft(1L);
    }

    @Test
    void createNewControlRangeSpecificationWithoutName() {
        assertThatException().isThrownBy(() -> ControlRangeSpecification.createNew(
                        null, "pH-3215", "pH", 6.5, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification name cannot be null or empty");

        assertThatException().isThrownBy(() -> ControlRangeSpecification.createNew(
                        "", "pH-3215", "pH", 6.5, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification name cannot be null or empty");
    }

    @Test
    void createNewControlRangeSpecificationWithoutMethod() {
        assertThatException().isThrownBy(() -> ControlRangeSpecification.createNew(
                        "PH", null, "pH", 6.5, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification method cannot be null or empty");
    }

    @Test
    void createNewControlRangeSpecificationWithoutUnit() {
        assertThatException().isThrownBy(() -> ControlRangeSpecification.createNew(
                        "PH", "pH-3215", null, 6.5, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification unit cannot be null or empty");
    }

    @Test
    void createNewControlRangeSpecificationWithoutComponentId() {
        assertThatException().isThrownBy(() -> ControlRangeSpecification.createNew(
                        "PH", "pH-3215", "pH", 6.5, 7.5, null, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification component ID cannot be null");
    }

    @Test
    void createNewControlRangeSpecificationWithInvalidRange() {
        assertThatException().isThrownBy(() -> ControlRangeSpecification.createNew(
                        "PH", "pH-3215", "pH", 8.0, 7.5, 1L, componentRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Control range specification minimum must be less than maximum");
    }

    @Test
    void updateControlRangeSpecification() {
        ControlRangeSpecification spec = createSpec();
        Mockito.clearInvocations(componentRepository);

        ControlRangeSpecification updated = spec.update("pH-7843", 6.0, 8.0, componentRepository);

        assertThat(updated.name()).isEqualTo("PH");
        assertThat(updated.method()).isEqualTo("pH-7843");
        assertThat(updated.min()).isEqualTo(6.0);
        assertThat(updated.max()).isEqualTo(8.0);
        assertThat(updated.componentId()).isEqualTo(1L); // inchangé
        assertThat(updated.status()).isEqualTo(ControlRangeSpecificationStatus.ACTIVE); // inchangé
        verify(componentRepository).setToDraft(1L);
    }

    @Test
    void deleteControlRangeSpecification() {
        ControlRangeSpecification spec = createSpec();
        Mockito.clearInvocations(componentRepository);

        ControlRangeSpecification deleted = spec.delete(componentRepository);

        assertThat(deleted.status()).isEqualTo(ControlRangeSpecificationStatus.DELETED);
        verify(componentRepository).setToDraft(1L);
    }

    @Test
    void deleteAlreadyDeletedControlRangeSpecificationShouldFail() {
        ControlRangeSpecification deleted = createSpec().delete(componentRepository);

        assertThatException().isThrownBy(() -> deleted.delete(componentRepository))
                .isInstanceOf(IllegalStateException.class)
                .withMessage("Control range specification is already deleted");
    }

    private ControlRangeSpecification createSpec() {
        return ControlRangeSpecification.createNew("PH", "pH-3215", "pH", 6.5, 7.5, 1L, componentRepository);
    }
}
