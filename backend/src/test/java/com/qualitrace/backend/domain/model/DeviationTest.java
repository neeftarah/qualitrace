package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.deviation.domain.model.Deviation;
import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class DeviationTest {

    @Test
    void createNewDeviationWithAllParameters() {
        Deviation deviation = Deviation.createNew(
                1L,
                "DEV-001",
                DeviationStatus.CLOSED,
                "Température hors limite corrigée"
        );

        assertThat(deviation.id()).isNull();
        assertThat(deviation.batchId()).isEqualTo(1L);
        assertThat(deviation.code()).isEqualTo("DEV-001");
        assertThat(deviation.status()).isEqualTo(DeviationStatus.CLOSED);
        assertThat(deviation.comment()).isEqualTo("Température hors limite corrigée");
    }

    @Test
    void createNewDeviationWithDefaultStatusAndComment() {
        Deviation deviation = Deviation.createNew(
                1L,
                "DEV-001",
                null,
                null
        );

        assertThat(deviation.id()).isNull();
        assertThat(deviation.batchId()).isEqualTo(1L);
        assertThat(deviation.code()).isEqualTo("DEV-001");
        assertThat(deviation.status()).isEqualTo(DeviationStatus.OPENED);
        assertThat(deviation.comment()).isEqualTo("");
    }

    @Test
    void createNewDeviationWithoutBatchId() {
        assertThatException().isThrownBy(() -> Deviation.createNew(
                        null,
                        "DEV-001",
                        DeviationStatus.OPENED,
                        "Comment"
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Batch ID cannot be null");
    }

    @Test
    void createNewDeviationWithoutCode() {
        assertThatException().isThrownBy(() -> Deviation.createNew(
                        1L,
                        null,
                        DeviationStatus.OPENED,
                        "Comment"
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Code cannot be null");
    }

    @Test
    void updateDeviation() {
        Deviation deviation = createDeviation();
        Deviation updated = deviation.update("Updated comment");

        assertThat(updated.id()).isNull();
        assertThat(updated.batchId()).isEqualTo(1L);
        assertThat(updated.code()).isEqualTo("DEV-202608-003");
        assertThat(updated.status()).isEqualTo(DeviationStatus.OPENED);
        assertThat(updated.comment()).isEqualTo("Updated comment");
    }

    @Test
    void updateDeviationWithoutCommentIsOK() {
        Deviation deviation = createDeviation();
        Deviation updated = deviation.update(null);

        assertThat(updated.id()).isNull();
        assertThat(updated.batchId()).isEqualTo(1L);
        assertThat(updated.code()).isEqualTo("DEV-202608-003");
        assertThat(updated.status()).isEqualTo(DeviationStatus.OPENED);
        assertThat(updated.comment()).isEqualTo(null);
    }

    @Test
    void changeStatuses() {
        Deviation deviation = createDeviation();
        assertThat(deviation.status()).isEqualTo(DeviationStatus.OPENED);

        // OPENED ==> OPENED
        assertThatException().isThrownBy(deviation::open).isInstanceOf(IllegalStateException.class)
                .withMessage("La déviation est déjà ouverte");

        // OPENED ==> CLOSED
        Deviation closedDeviation = deviation.close();
        assertThat(closedDeviation.status()).isEqualTo(DeviationStatus.CLOSED);

        // CLOSED ==> CLOSED
        assertThatException().isThrownBy(closedDeviation::close).isInstanceOf(IllegalStateException.class)
                .withMessage("La déviation est déjà fermée");

        // CLOSED ==> OPENED
        Deviation reopenedDeviation = closedDeviation.open();
        assertThat(reopenedDeviation.status()).isEqualTo(DeviationStatus.OPENED);
    }

    private Deviation createDeviation() {
        return Deviation.createNew(
                1L,
                "DEV-202608-003",
                DeviationStatus.OPENED,
                "This is a test deviation."
        );
    }
}
