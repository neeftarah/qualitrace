INSERT INTO components (id, type, reference, name, status, available_from, supplier_id) VALUES
 (201, 'RAW_MATERIAL', 'RM-ACID-CITRIC', 'Acide citrique anhydre', 'ACTIVE', '2026-01-15T08:00:00Z', 101),
 (202, 'RAW_MATERIAL', 'RM-SODIUM-HYD', 'Hydroxyde de sodium', 'ACTIVE', '2026-02-03T08:00:00Z', 101),
 (203, 'RAW_MATERIAL', 'RM-LACTOSE-01', 'Lactose monohydraté', 'ACTIVE', '2026-02-18T08:00:00Z', 102),
 (204, 'COMPONENT', 'CP-CAP-010', 'Capsule taille 0', 'ACTIVE', '2026-03-01T08:00:00Z', 103),
 (205, 'COMPONENT', 'CP-BLEND-042', 'Mélange granulé 042', 'DRAFT', NULL, 102),
 (206, 'RAW_MATERIAL', 'RM-OLD-SOLV', 'Solvant historique', 'ARCHIVED', NULL, 104);

SELECT setval(pg_get_serial_sequence('components', 'id'), 206, true);
