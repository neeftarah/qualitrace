package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.domain.type.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class AnalysisResultTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.createNew("jdoe", "password", "jdoe@example.com", "John", "Doe", Set.of(UserRole.CQ));
    }

    @Test
    void createNewAnalysisResult() {
        AnalysisResult result = AnalysisResult.createNew(1L, 2L, 7.5, user);

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
                        user
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Batch ID cannot be null");
    }

    @Test
    void createNewAnalysisResultWithoutSpecificationId() {
        assertThatException().isThrownBy(() -> AnalysisResult.createNew(
                        1L,
                        null,
                        7.5,
                        user
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Specification ID cannot be null");
    }

    @Test
    void createNewAnalysisResultWithoutValue() {
        assertThatException().isThrownBy(() -> AnalysisResult.createNew(
                        1L,
                        2L,
                        null,
                        user
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Value cannot be null");
    }

    @Test
    void createNewAnalysisResultWithoutCreatedBy() {
        assertThatException().isThrownBy(() -> AnalysisResult.createNew(
                        1L,
                        2L,
                        7.5,
                        null
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Creator user cannot be null");
    }
}
