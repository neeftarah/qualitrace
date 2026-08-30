package com.qualitrace.backend.batch.infrastructure.pdf;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperPrint;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JasperTemplateLoadTest {
    @Test
    void templateCompiles() throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("templates/pdf/batch-certificate.jrxml")) {
            assertNotNull(input);
            JasperReport report = JasperCompileManager.compileReport(input);
            Map<String, Object> parameters = new HashMap<>(Map.of(
                    "INTERNAL_REF", "LOT-001",
                    "SUPPLIER_REF", "SUP-001",
                    "EXPIRY_DATE", "2030-12-31",
                    "RECEPTION_DATE", "2026-01-01",
                    "STATUS", "RELEASED",
                    "VALIDATED_AT", "30/08/2026",
                    "VALIDATED_BY", "Alice Dupont",
                    "COMPONENT_NAME", "Component",
                    "SPECIFICATIONS_DATA_SOURCE", new JRBeanCollectionDataSource(java.util.List.of()),
                    "DEVIATIONS_DATA_SOURCE", new JRBeanCollectionDataSource(java.util.List.of())
            ));
            JasperPrint print = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());

            assertTrue(JasperExportManager.exportReportToPdf(print).length > 4);
        }
    }
}
