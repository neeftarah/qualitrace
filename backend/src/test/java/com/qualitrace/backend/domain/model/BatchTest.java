package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.analysisresult.domain.repository.AnalysisResultRepository;
import com.qualitrace.backend.analysisresult.domain.type.AnalysisResultStatus;
import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.specification.domain.model.Specification;
import com.qualitrace.backend.specification.domain.repository.SpecificationRepository;
import com.qualitrace.backend.specification.domain.type.SpecificationStatus;
import com.qualitrace.backend.deviation.domain.repository.DeviationRepository;
import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.domain.type.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private DeviationRepository deviationRepository;

    @Mock
    private AnalysisResultRepository analysisRepository;

    @Mock
    private SpecificationRepository controlRepository;

    private Supplier supplier;
    private Component activeComponent;

    @BeforeEach
    void setUp() {
        supplier = Supplier.createNew("SUP001", "Supplier 1", "123 Main St, City, Country");
        activeComponent = new Component(
                1L,
                ComponentType.RAW_MATERIAL,
                "COMP-001",
                "Component 1",
                Instant.now(),
                ComponentStatus.ACTIVE,
                supplier
        );
    }

    @Test
    void createNewBatch() {
        when(batchRepository.nextInternalReferenceNumber(eq(ComponentType.RAW_MATERIAL), any(Instant.class)))
                .thenReturn("LOT-2026-001");

        Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);
        Batch batch = Batch.createNew(activeComponent, "SUP-LOT-999", expiryDate, batchRepository);

        assertThat(batch.id()).isNull();
        assertThat(batch.component()).isEqualTo(activeComponent);
        assertThat(batch.internalBatchNumber()).isEqualTo("LOT-2026-001");
        assertThat(batch.supplierBatchNumber()).isEqualTo("SUP-LOT-999");
        assertThat(batch.expiryDate()).isEqualTo(expiryDate);
        assertThat(batch.receptionDate()).isNotNull();
        assertThat(batch.status()).isEqualTo(BatchStatus.QUARANTINE);
    }

    @Test
    void createNewBatchWithoutComponent() {
        Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

        assertThatException().isThrownBy(() -> Batch.createNew(
                        null,
                        "SUP-LOT-999",
                        expiryDate,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component cannot be null");
    }

    @Test
    void createNewBatchWithoutSupplierReferenceNumber() {
        Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

        assertThatException().isThrownBy(() -> Batch.createNew(
                        activeComponent,
                        null,
                        expiryDate,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier reference number cannot be null or empty");

        assertThatException().isThrownBy(() -> Batch.createNew(
                        activeComponent,
                        "",
                        expiryDate,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier reference number cannot be null or empty");

        assertThatException().isThrownBy(() -> Batch.createNew(
                        activeComponent,
                        "   ",
                        expiryDate,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Supplier reference number cannot be null or empty");
    }

    @Test
    void createNewBatchWithoutExpiryDate() {
        assertThatException().isThrownBy(() -> Batch.createNew(
                        activeComponent,
                        "SUP-LOT-999",
                        null,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Expiry date cannot be null");
    }

    @Test
    void createNewBatchWithInactiveComponentShouldFail() {
        Instant expiryDate = Instant.now().plus(30, ChronoUnit.DAYS);

        Component draftComponent = new Component(
                1L,
                ComponentType.RAW_MATERIAL,
                "COMP-001",
                "Component 1",
                null,
                ComponentStatus.DRAFT,
                supplier
        );

        assertThatException().isThrownBy(() -> Batch.createNew(
                        draftComponent,
                        "SUP-LOT-999",
                        expiryDate,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component must be active");

        Component archivedComponent = new Component(
                1L,
                ComponentType.RAW_MATERIAL,
                "COMP-001",
                "Component 1",
                null,
                ComponentStatus.ARCHIVED,
                supplier
        );

        assertThatException().isThrownBy(() -> Batch.createNew(
                        archivedComponent,
                        "SUP-LOT-999",
                        expiryDate,
                        batchRepository
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("Component must be active");
    }

    @Test
    void supplier() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);
        assertThat(batch.supplier()).isEqualTo(supplier);

        Batch batchWithoutComponent = new Batch(
                1L,
                null,
                "LOT-001",
                "SUP-LOT-001",
                Instant.now().plus(30, ChronoUnit.DAYS),
                Instant.now(),
                BatchStatus.QUARANTINE
        );
        assertThat(batchWithoutComponent.supplier()).isNull();
    }

    @Test
    void getAnalysisStatusPendingWhenNoResults() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);
        when(analysisRepository.findAllByBatchId(1L)).thenReturn(List.of());
        when(controlRepository.findByComponent(1L)).thenReturn(List.of(
                createSpecification(10L, "PH")
        ));

        AnalysisResultStatus status = batch.getAnalysisStatus(analysisRepository, controlRepository);
        assertThat(status).isEqualTo(AnalysisResultStatus.PENDING);
    }

    @Test
    void getAnalysisStatusCompletedWhenAllResultsProvided() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);
        when(analysisRepository.findAllByBatchId(1L)).thenReturn(List.of(
                new AnalysisResult(100L, 1L, 10L, 7.0, Instant.now(), null),
                new AnalysisResult(101L, 1L, 11L, 99.5, Instant.now(), null)
        ));
        when(controlRepository.findByComponent(1L)).thenReturn(List.of(
                createSpecification(10L, "PH"),
                createSpecification(11L, "Purity")
        ));

        AnalysisResultStatus status = batch.getAnalysisStatus(analysisRepository, controlRepository);
        assertThat(status).isEqualTo(AnalysisResultStatus.COMPLETED);
    }

    @Test
    void getAnalysisStatusInProgressWhenPartialResultsProvided() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);
        when(analysisRepository.findAllByBatchId(1L)).thenReturn(List.of(
                new AnalysisResult(100L, 1L, 10L, 7.0, Instant.now(), null)
        ));
        when(controlRepository.findByComponent(1L)).thenReturn(List.of(
                createSpecification(10L, "PH"),
                createSpecification(11L, "Purity")
        ));

        AnalysisResultStatus status = batch.getAnalysisStatus(analysisRepository, controlRepository);
        assertThat(status).isEqualTo(AnalysisResultStatus.IN_PROGRESS);
    }

    @Test
    void hasOpenDeviations() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);

        when(deviationRepository.existsByBatchIdAndStatus(1L, DeviationStatus.OPENED)).thenReturn(true);
        assertThat(batch.hasOpenDeviations(deviationRepository)).isTrue();

        when(deviationRepository.existsByBatchIdAndStatus(1L, DeviationStatus.OPENED)).thenReturn(false);
        assertThat(batch.hasOpenDeviations(deviationRepository)).isFalse();
    }

    @Test
    void validateAccepted() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);

        when(analysisRepository.findAllByBatchId(1L)).thenReturn(List.of(
                new AnalysisResult(100L, 1L, 10L, 7.0, Instant.now(), null)
        ));
        when(controlRepository.findByComponent(1L)).thenReturn(List.of(
                createSpecification(10L, "PH")
        ));
        when(deviationRepository.existsByBatchIdAndStatus(1L, DeviationStatus.OPENED)).thenReturn(false);

        Batch validated = batch.validate(true, deviationRepository, analysisRepository, controlRepository);

        assertThat(validated.status()).isEqualTo(BatchStatus.RELEASED);
        assertThat(validated.id()).isEqualTo(1L);
        assertThat(validated.component()).isEqualTo(activeComponent);
    }

    @Test
    void validateRefused() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);

        when(analysisRepository.findAllByBatchId(1L)).thenReturn(List.of(
                new AnalysisResult(100L, 1L, 10L, 7.0, Instant.now(), null)
        ));
        when(controlRepository.findByComponent(1L)).thenReturn(List.of(
                createSpecification(10L, "PH")
        ));
        when(deviationRepository.existsByBatchIdAndStatus(1L, DeviationStatus.OPENED)).thenReturn(false);

        Batch validated = batch.validate(false, deviationRepository, analysisRepository, controlRepository);

        assertThat(validated.status()).isEqualTo(BatchStatus.REJECTED);
    }

    @Test
    void validateStoresValidatorAndValidationDate() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);
        User validator = User.createNew(
                "alice", "password", "alice@example.com", "Alice", "Dupont", Set.of(UserRole.AQ));
        Instant validationDate = Instant.parse("2026-08-30T10:15:30Z");

        when(analysisRepository.findAllByBatchId(1L)).thenReturn(List.of(
                new AnalysisResult(100L, 1L, 10L, 7.0, Instant.now(), null)
        ));
        when(controlRepository.findByComponent(1L)).thenReturn(List.of(
                createSpecification(10L, "PH")
        ));
        when(deviationRepository.existsByBatchIdAndStatus(1L, DeviationStatus.OPENED)).thenReturn(false);

        Batch validated = batch.validate(
                true, deviationRepository, analysisRepository, controlRepository, validator, validationDate);

        assertThat(validated.status()).isEqualTo(BatchStatus.RELEASED);
        assertThat(validated.validatedBy()).isEqualTo(validator);
        assertThat(validated.validatedAt()).isEqualTo(validationDate);
    }

    @Test
    void validateWhenNotQuarantineShouldFail() {
        Batch receivedBatch = createBatch(1L, BatchStatus.RELEASED);

        assertThatException().isThrownBy(() -> receivedBatch.validate(
                        true, deviationRepository, analysisRepository, controlRepository
                )).isInstanceOf(IllegalStateException.class)
                .withMessage("Seul un composant en quarantaine peut être validé");
    }

    @Test
    void validateWhenAnalysisNotCompletedShouldFail() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);

        when(analysisRepository.findAllByBatchId(1L)).thenReturn(List.of());
        when(controlRepository.findByComponent(1L)).thenReturn(List.of(
                createSpecification(10L, "PH")
        ));

        assertThatException().isThrownBy(() -> batch.validate(
                        true, deviationRepository, analysisRepository, controlRepository
                )).isInstanceOf(IllegalStateException.class)
                .withMessage("Tous les résultats d'analyses doivent avoir été saisis pour valider un lot");
    }

    @Test
    void validateWhenHasOpenDeviationsShouldFail() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);

        when(analysisRepository.findAllByBatchId(1L)).thenReturn(List.of(
                new AnalysisResult(100L, 1L, 10L, 7.0, Instant.now(), null)
        ));
        when(controlRepository.findByComponent(1L)).thenReturn(List.of(
                createSpecification(10L, "PH")
        ));
        when(deviationRepository.existsByBatchIdAndStatus(1L, DeviationStatus.OPENED)).thenReturn(true);

        assertThatException().isThrownBy(() -> batch.validate(
                        true, deviationRepository, analysisRepository, controlRepository
                )).isInstanceOf(IllegalStateException.class)
                .withMessage("Toutes les déviations doivent être clôturées avant de pouvoir valider un lot");
    }

    @Test
    void useBatch() {
        Batch batch = createBatch(1L, BatchStatus.RELEASED);
        Batch usedBatch = batch.use();

        assertThat(usedBatch.status()).isEqualTo(BatchStatus.USED);
    }

    @Test
    void useBatchWhenNotReceivedShouldFail() {
        Batch batch = createBatch(1L, BatchStatus.QUARANTINE);

        assertThatException().isThrownBy(batch::use)
                .isInstanceOf(IllegalStateException.class)
                .withMessage("Seul un lot validé peu être utilisé");
    }

    @Test
    void destroyBatch() {
        Batch batch = createBatch(1L, BatchStatus.REJECTED);
        Batch destroyedBatch = batch.destroy();

        assertThat(destroyedBatch.status()).isEqualTo(BatchStatus.DESTROYED);
    }

    @Test
    void destroyAlreadyDestroyedBatchShouldFail() {
        Batch batch = createBatch(1L, BatchStatus.DESTROYED);

        assertThatException().isThrownBy(batch::destroy)
                .isInstanceOf(IllegalStateException.class)
                .withMessage("Le lot est déjà détruit");
    }

    @Test
    void isExpired() {
        Batch expiredBatch = new Batch(
                1L,
                activeComponent,
                "LOT-001",
                "SUP-LOT-001",
                Instant.now().minus(2, ChronoUnit.DAYS),
                Instant.now().minus(5, ChronoUnit.DAYS),
                BatchStatus.RELEASED
        );
        assertThat(expiredBatch.isExpired()).isTrue();

        Batch validBatch = new Batch(
                2L,
                activeComponent,
                "LOT-002",
                "SUP-LOT-002",
                Instant.now().plus(2, ChronoUnit.DAYS),
                Instant.now(),
                BatchStatus.RELEASED
        );
        assertThat(validBatch.isExpired()).isFalse();
    }

    private Batch createBatch(Long id, BatchStatus status) {
        return new Batch(
                id,
                activeComponent,
                "LOT-2026-001",
                "SUP-LOT-001",
                Instant.now().plus(30, ChronoUnit.DAYS),
                Instant.now(),
                status
        );
    }

    private Specification createSpecification(Long id, String name) {
        return new Specification(
                id,
                name,
                "METHOD-01",
                "unit",
                5.0,
                10.0,
                SpecificationStatus.ACTIVE,
                1L
        );
    }
}
