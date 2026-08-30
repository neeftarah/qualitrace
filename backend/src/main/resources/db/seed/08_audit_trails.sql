INSERT INTO audit_trail (id, author_id, event, entity_type, entity_id, timestamp, previous_data, changed_data) VALUES
 (701, '00bd0db5-e97a-4590-991d-9ceb82911acd', 'CREATED', 'SUPPLIER', '101', '2026-01-05T08:00:00Z', '{}'::jsonb, '{"code":"SUP-ALP","name":"Alpine Ingredients","status":"ACTIVE"}'::jsonb),
 (702, '00bd0db5-e97a-4590-991d-9ceb82911acd', 'CREATED', 'COMPONENT', '201', '2026-01-15T08:00:00Z', '{}'::jsonb, '{"reference":"RM-ACID-CITRIC","status":"ACTIVE","supplierId":101}'::jsonb),
 (703, '00bd0db5-e97a-4590-991d-9ceb82911acd', 'CREATED', 'BATCH', '401', '2026-01-20T09:15:00Z', '{}'::jsonb, '{"internalBatchNumber":"LOT-MP-202601-001","status":"QUARANTINE"}'::jsonb),
 (704, '1097b08e-7ccb-46eb-b784-d29036e2860e', 'VALIDATED', 'BATCH', '401', '2026-01-22T14:30:00Z', '{"status":"QUARANTINE"}'::jsonb, '{"status":"RELEASED","validatedBy":"1097b08e-7ccb-46eb-b784-d29036e2860e"}'::jsonb),
 (705, 'b9381ebf-114b-4127-a00f-763bded71eaf', 'CREATED', 'ANALYSIS_RESULT', '501', '2026-01-21T10:00:00Z', '{}'::jsonb, '{"batchId":401,"specificationId":301,"value":99.70}'::jsonb),
 (706, '1097b08e-7ccb-46eb-b784-d29036e2860e', 'CREATED', 'DEVIATION', '601', '2026-02-23T16:10:00Z', '{}'::jsonb, '{"batchId":403,"code":"DEV-SOD-001","status":"OPENED"}'::jsonb);

SELECT setval(pg_get_serial_sequence('audit_trail', 'id'), 706, true);
