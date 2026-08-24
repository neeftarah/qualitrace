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
}
