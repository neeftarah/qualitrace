INSERT INTO analysis_results (id, batch_id, specification_id, value, created_at, created_by) VALUES
 (501, 401, 301, 99.70, '2026-01-21T10:00:00Z', 'b9381ebf-114b-4127-a00f-763bded71eaf'),
 (502, 401, 302, 0.22, '2026-01-21T10:05:00Z', 'b9381ebf-114b-4127-a00f-763bded71eaf'),
 (503, 402, 301, 99.55, '2026-02-13T10:30:00Z', 'b9381ebf-114b-4127-a00f-763bded71eaf'),
 (504, 402, 302, 0.31, '2026-02-13T10:35:00Z', 'b9381ebf-114b-4127-a00f-763bded71eaf'),
 (505, 404, 304, 97.20, '2026-03-06T09:20:00Z', 'b9381ebf-114b-4127-a00f-763bded71eaf'),
 (506, 405, 305, 1.00, '2026-03-12T08:30:00Z', 'b9381ebf-114b-4127-a00f-763bded71eaf'),
 (507, 408, 305, 1.00, '2026-05-07T09:15:00Z', 'b9381ebf-114b-4127-a00f-763bded71eaf');

SELECT setval(pg_get_serial_sequence('analysis_results', 'id'), 507, true);
