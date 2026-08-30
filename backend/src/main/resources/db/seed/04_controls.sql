INSERT INTO control_range_specifications (id, component_id, name, method, unit, min, max, status) VALUES
 (301, 201, 'Pureté', 'Chromatographie HPLC', '%', 99.00, 100.00, 'ACTIVE'),
 (302, 201, 'Humidité', 'Karl Fischer', '%', 0.00, 0.50, 'ACTIVE'),
 (303, 202, 'Concentration', 'Titrage acido-basique', '%', 48.00, 52.00, 'ACTIVE'),
 (304, 203, 'Granulométrie', 'Tamisage', '% passant', 95.00, 100.00, 'ACTIVE'),
 (305, 204, 'Aspect', 'Contrôle visuel', 'conforme', 1.00, 1.00, 'ACTIVE'),
 (306, 205, 'Masse moyenne', 'Pesée statistique', 'mg', 95.00, 105.00, 'ACTIVE'),
 (307, 206, 'Identité', 'Spectrométrie IR', 'conforme', 1.00, 1.00, 'DELETED');

SELECT setval(pg_get_serial_sequence('control_range_specifications', 'id'), 307, true);
