package com.qualitrace.backend.batch.infrastructure.pdf;

import com.qualitrace.backend.batch.application.dto.BatchDetailResponse;
import com.qualitrace.backend.component.domain.type.ComponentType;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    .withZone(ZoneId.systemDefault());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("LOGO", new ClassPathResource("templates/pdf/logo.png").getInputStream());
            parameters.put("COMPONENT_NAME", batch.component().name());


            parameters.put("COMPONENT_TYPE", (batch.component().type() == ComponentType.COMPONENT) ? "Composant d'emballage" : "Matière première");
            parameters.put("INTERNAL_REF", batch.internalBatchNumber());
            parameters.put("RECEPTION_DATE", batch.receptionDate() != null ? formatter.format(batch.receptionDate()) : "");
            parameters.put("EXPIRY_DATE", batch.expiryDate() != null ? formatter.format(batch.expiryDate()) : "");

            parameters.put("COMPONENT_REF", batch.component().reference());
            parameters.put("SUPPLIER_NAME", batch.component().supplier().name());
            parameters.put("SUPPLIER_CODE", batch.component().supplier().code());
            parameters.put("SUPPLIER_REF", batch.supplierBatchNumber());

            parameters.put("STATUS", batch.status() != null ? batch.status().name() : "");
            parameters.put("VALIDATED_AT", batch.validatedAt() != null ? formatter.format(batch.validatedAt()) : "");
            parameters.put("VALIDATED_BY", batch.validatedBy() != null ? batch.validatedBy() : "");
            parameters.put("HAS_DEVIATIONS", !batch.deviations().isEmpty());

            // 4. Conversion des listes d'objets (Specifications & Deviations) en DataSources Jasper
            List<Map<String, ?>> specificationRows = new ArrayList<>();
            batch.specifications().forEach(specification -> {
                Double measuredValue = specification.results() != null
                        ? specification.results().value()
                        : null;
                Map<String, Object> values = new HashMap<>();
                values.put("name", specification.name());
                values.put("method", specification.method());
                values.put("unit", specification.unit());
                values.put("min", specification.min());
                values.put("max", specification.max());
                values.put("measuredValue", measuredValue);
                values.put("status", measuredValue != null
                        && specification.min() != null
                        && specification.max() != null
                        && measuredValue >= specification.min()
                        && measuredValue <= specification.max()
                        ? "CONFORME"
                        : "NON CONFORME");
                specificationRows.add(values);
            });
            JRMapCollectionDataSource specificationsDS = new JRMapCollectionDataSource(specificationRows);
            // DeviationResponse est un record : il expose code() et non getCode().
            // JRBeanCollectionDataSource ne sait pas introspecter les accesseurs de records.
            List<Map<String, ?>> deviationRows = new ArrayList<>();
            batch.deviations().forEach(deviation -> {
                Map<String, Object> values = new HashMap<>();
                values.put("code", deviation.code());
                values.put("comment", deviation.comment());
                values.put("status", deviation.status() != null
                        ? (deviation.status().name().equals("OPENED") ? "OUVERTE" : "FERMÉE")
                        : "");
                deviationRows.add(values);
            });
            JRMapCollectionDataSource deviationsDS = new JRMapCollectionDataSource(
                    deviationRows
            );

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
