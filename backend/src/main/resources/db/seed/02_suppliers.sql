INSERT INTO suppliers (id, code, name, address, status) VALUES
 (101, 'SUP-ALP', 'Alpine Ingredients', '12 rue des Alpes, 69007 Lyon', 'ACTIVE'),
 (102, 'SUP-NOV', 'Novatech Components', '8 avenue des Sciences, 31000 Toulouse', 'ACTIVE'),
 (103, 'SUP-ATL', 'Atlantique Packaging', '45 boulevard du Port, 44000 Nantes', 'ACTIVE'),
 (104, 'SUP-ARCH', 'Archive Chemicals', '3 chemin Industriel, 67000 Strasbourg', 'ARCHIVED');

SELECT setval(pg_get_serial_sequence('suppliers', 'id'), 104, true);
