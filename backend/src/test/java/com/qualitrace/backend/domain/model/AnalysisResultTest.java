package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.domain.type.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalysisResultTest {

    private User user;
    @Mock
    private BatchRepository batchRepository;

    @BeforeEach
    void setUp() {
        user = User.createNew("jdoe", "password", "jdoe@example.com", "John", "Doe", Set.of(UserRole.CQ));
        when(batchRepository.findById(1L)).thenReturn(Optional.of(new Batch(1L, null, null, null, null, null, BatchStatus.QUARANTINE)));
    }

    @Test
    void createNewAnalysisResult() {
        AnalysisResult result = AnalysisResult.createNew(1L, 2L, 7.5, user, batchRepository);

        assertThat(result.id()).isNull();
        assertThat(result.batchId()).isEqualTo(1L);
        assertThat(result.specificationId()).isEqualTo(2L);
        assertThat(result.value()).isEqualTo(7.5);
        assertThat(result.createdBy()).isEqualTo(user);
        assertThat(result.createdAt()).isNotNull();
    }

    @Test
    void createNewAnalysisResultWithoutBatchId() {
        assertThatException().isThrownBy(() -> AnalysisResult.createNew(
                        null,
                        2L,
                        7.5,
                        user,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Batch ID cannot be null");
    }

    @Test
    void createNewAnalysisResultWithoutSpecificationId() {
        assertThatException().isThrownBy(() -> AnalysisResult.createNew(
                        1L,
                        null,
                        7.5,
                        user,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Specification ID cannot be null");
    }

    @Test
    void createNewAnalysisResultWithoutValue() {
        assertThatException().isThrownBy(() -> AnalysisResult.createNew(
                        1L,
                        2L,
                        null,
                        user,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Value cannot be null");
    }

    @Test
    void createNewAnalysisResultWithoutCreatedBy() {
        assertThatException().isThrownBy(() -> AnalysisResult.createNew(
                        1L,
                        2L,
                        7.5,
                        null,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Creator user cannot be null");
    }

    @Test
    void updateShouldChangeValueAndPreserveIdentityAndCreator() {
        AnalysisResult result = AnalysisResult.createNew(1L, 2L, 7.5, user, batchRepository);

        AnalysisResult updated = result.update(8.25, batchRepository);

        assertThat(updated.id()).isEqualTo(result.id());
        assertThat(updated.batchId()).isEqualTo(result.batchId());
        assertThat(updated.specificationId()).isEqualTo(result.specificationId());
        assertThat(updated.value()).isEqualTo(8.25);
        assertThat(updated.createdAt()).isEqualTo(result.createdAt());
        assertThat(updated.createdBy()).isEqualTo(user);
    }

    @Test
    void createShouldBeRejectedWhenBatchIsNotInQuarantine() {
        when(batchRepository.findById(1L)).thenReturn(Optional.of(new Batch(1L, null, null, null, null, null, BatchStatus.RELEASED)));

        assertThatException().isThrownBy(() -> AnalysisResult.createNew(1L, 2L, 7.5, user, batchRepository))
                .isInstanceOf(IllegalArgumentException.class)
                .withMessage("The batch must not have been validated");
    }

    @Test
    void updateShouldBeRejectedWhenBatchIsNotInQuarantine() {
        AnalysisResult result = AnalysisResult.createNew(1L, 2L, 7.5, user, batchRepository);
        when(batchRepository.findById(1L)).thenReturn(Optional.of(new Batch(1L, null, null, null, null, null, BatchStatus.RELEASED)));

        assertThatException().isThrownBy(() -> result.update(8.25, batchRepository))
                .isInstanceOf(IllegalArgumentException.class)
                .withMessage("The batch must not have been validated");
    }
}
