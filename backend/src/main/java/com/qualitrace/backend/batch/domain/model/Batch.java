package com.qualitrace.backend.batch.domain.model;

import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.analysisresult.domain.repository.AnalysisResultRepository;
import com.qualitrace.backend.analysisresult.domain.type.AnalysisResultStatus;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.controls.domain.model.ControlRangeSpecification;
import com.qualitrace.backend.controls.domain.repository.ControlRangeSpecificationRepository;
import com.qualitrace.backend.deviation.domain.repository.DeviationRepository;
import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import com.qualitrace.backend.supplier.domain.model.Supplier;

import java.time.Instant;
import java.util.List;

public record Batch(
        Long id,
        Component component,
        String internalReferenceNumber,
        String supplierReferenceNumber,
        Instant expiryDate,
        Instant receptionDate,
        BatchStatus status
) {
    public static Batch createNew(
            Component component,
            String supplierReferenceNumber,
            Instant expiryDate,
            BatchRepository batchRepository
    ) {
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }
        if (supplierReferenceNumber == null || supplierReferenceNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier reference number cannot be null or empty");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date cannot be null");
        }

        // RG-STK-05 : Aucune commande ne peut être réalisée sur une matière première « non disponible » (RG-REF-05)
        if (component.status() != ComponentStatus.ACTIVE) {
            throw new IllegalArgumentException("Component must be active");
        }

        // UC-STK-02 : Le système enregistre la date et l'heure de réception via l'heure serveur (RG-TRAC-08).
        Instant receptionDate = Instant.now();

        // RG-STK-02 : Génération automatique du numéro de lot interne
        String internalRef = batchRepository.nextInternalReferenceNumber(component.type(), receptionDate);


        return new Batch(
                null, // Placeholder ID, will be replaced by the repository
                component,
                internalRef,
                supplierReferenceNumber,
                expiryDate,
                receptionDate,
                BatchStatus.QUARANTINE // RG-STK-01 : Tout lot créé lors d'une réception est obligatoirement en statut « Quarantaine ».
        );
    }

    public Supplier supplier() {
        return component != null ? component.supplier() : null;
    }

    public Batch validate(
            boolean accept,
            DeviationRepository deviationRepository,
            AnalysisResultRepository analysisRepository,
            ControlRangeSpecificationRepository controlRepository
    ) {
        if (this.status != BatchStatus.QUARANTINE) {
            throw new IllegalStateException("Seul un composant en quarantaine peut être validé");
        }
        if (this.getAnalysisStatus(analysisRepository, controlRepository) != AnalysisResultStatus.COMPLETED) {
            throw new IllegalStateException("Tous les résultats d'analyses doivent avoir été saisis pour valider un lot");
        }
        if (hasOpenDeviations(deviationRepository)) {
            throw new IllegalStateException("Toutes les déviations doivent être clôturées avant de pouvoir valider un lot");
        }

        return withStatus(accept ? BatchStatus.RECEIVED : BatchStatus.REFUSED);
    }

    public AnalysisResultStatus getAnalysisStatus(
            AnalysisResultRepository analysisRepository,
            ControlRangeSpecificationRepository controlRepository
    ) {
        List<AnalysisResult> results = analysisRepository.findAllByBatchId(this.id);
        List<ControlRangeSpecification> controls = controlRepository.findByComponent(this.component.id());

        if (results.isEmpty()) {
            return AnalysisResultStatus.PENDING;
        } else if (results.size() == controls.size()) {
            return AnalysisResultStatus.COMPLETED;
        } else {
            return AnalysisResultStatus.IN_PROGRESS;
        }
    }

    public boolean hasOpenDeviations(DeviationRepository deviationRepository) {
        return deviationRepository.existsByBatchIdAndStatus(this.id, DeviationStatus.OPENED);
    }

    public Batch use() {
        if (this.status != BatchStatus.RECEIVED) {
            throw new IllegalStateException("Seul un lot validé peu être utilisé");
        }

        return withStatus(BatchStatus.USED);
    }

    public Batch destroy() {
        if (this.status == BatchStatus.DESTROYED) {
            throw new IllegalStateException("Le composant est déjà détruit");
        }

        return withStatus(BatchStatus.DESTROYED);
    }

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

    private Batch withStatus(BatchStatus newStatus) {
        return new Batch(
                this.id,
                this.component,
                this.internalReferenceNumber,
                this.supplierReferenceNumber,
                this.expiryDate,
                this.receptionDate,
                newStatus
        );
    }
}
