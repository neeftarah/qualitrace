INSERT INTO deviations (id, batch_id, code, status, comment) VALUES
 (601, 403, 'DEV-SOD-001', 'OPENED', 'Concentration mesurée inférieure à la spécification.'),
 (602, 403, 'DEV-SOD-002', 'CLOSED', 'Écart confirmé après contre-analyse ; lot refusé.'),
 (603, 406, 'DEV-CIT-001', 'CLOSED', 'Conditionnement endommagé à la réception ; lot détruit.'),
 (604, 402, 'DEV-CIT-002', 'OPENED', 'Échantillon complémentaire demandé par le laboratoire.');

SELECT setval(pg_get_serial_sequence('deviations', 'id'), 604, true);
