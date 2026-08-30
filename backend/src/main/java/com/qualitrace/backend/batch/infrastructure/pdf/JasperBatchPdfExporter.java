package com.qualitrace.backend.batch.infrastructure.pdf;

import com.qualitrace.backend.batch.application.dto.BatchDetailResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class JasperBatchPdfExporter {

    private static final String TEMPLATE_PATH = "templates/pdf/batch-certificate.jrxml";

    public byte[] generateBatchReport(BatchDetailResponse batch) {
        InputStream templateStream;
        JasperReport jasperReport;

        // 1. Chargement du fichier template .jrxml depuis classpath (src/main/resources)
        try {
            templateStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(TEMPLATE_PATH);

            if (templateStream == null) {
                throw new IllegalStateException(
                        "Ressource introuvable sur le ClassLoader : " + TEMPLATE_PATH +
                                " - Vérifiez la présence du fichier dans build/resources/main/"
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Chargement du template PDF impossible : " + TEMPLATE_PATH, e);
        }

        // 2. Compilation du template JRXML en mémoire
        try {
            jasperReport = JasperCompileManager.compileReport(templateStream);
        } catch (Exception e) {
            throw new RuntimeException("Compilation du template PDF impossible : " + e.getMessage(), e);
        }

        // 3. Préparation des paramètres pour le rapport principal
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("BATCH_ID", batch.id());
            parameters.put("INTERNAL_REF", batch.internalBatchNumber());
            parameters.put("SUPPLIER_REF", batch.supplierBatchNumber());
            parameters.put("EXPIRY_DATE", batch.expiryDate() != null ? batch.expiryDate().toString() : "");
            parameters.put("RECEPTION_DATE", batch.receptionDate() != null ? batch.receptionDate().toString() : "");
            parameters.put("STATUS", batch.status() != null ? batch.status().name() : "");

            // Traitement de l'objet imbriqué Component
            if (batch.component() != null) {
                parameters.put("COMPONENT_NAME", batch.component().name()); // à adapter selon votre ComponentResponse
            }

            // 4. Conversion des listes d'objets (Specifications & Deviations) en DataSources Jasper
            JRBeanCollectionDataSource specificationsDS = new JRBeanCollectionDataSource(batch.specifications());
            JRBeanCollectionDataSource deviationsDS = new JRBeanCollectionDataSource(batch.deviations());

            parameters.put("SPECIFICATIONS_DATA_SOURCE", specificationsDS);
            parameters.put("DEVIATIONS_DATA_SOURCE", deviationsDS);

            // 5. Remplissage du rapport (EmptyDataSource pour le document principal, les données passent par les paramètres)
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // 6. Export au format PDF (byte array)
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du rapport PDF du lot " + batch.id(), e);
        }
    }
}