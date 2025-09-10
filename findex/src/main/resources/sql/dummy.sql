BEGIN;

-- (선택) 이전에 넣었던 DEMO 데이터가 있으면 정리하고 싶을 때 주석 해제
DELETE
FROM auto_sync_configs
WHERE index_info_id IN (SELECT id FROM index_infos WHERE index_name LIKE 'DEMO-INDEX-%');
DELETE
FROM index_data
WHERE index_info_id IN (SELECT id FROM index_infos WHERE index_name LIKE 'DEMO-INDEX-%');
DELETE
FROM sync_jobs
WHERE (index_info_id IN (SELECT id FROM index_infos WHERE index_name LIKE 'DEMO-INDEX-%') OR index_info_id IS NULL);
DELETE
FROM index_infos
WHERE index_name LIKE 'DEMO-INDEX-%';

-- 1) index_infos 50개
INSERT INTO index_infos
(index_name, index_classification, employed_items_count, base_point_in_time, base_index, source_type, favorite)
SELECT format('DEMO-INDEX-%02s', gs)                                           AS index_name,
       (ARRAY ['KOSPI시리즈','KOSDAQ시리즈','KRX100','테마'])[(random() * 3)::int + 1] AS index_classification,
       (50 + (random() * 450)::int)                                            AS employed_items_count,
       DATE '2000-01-01' + ((random() * 7000)::int)                            AS base_point_in_time,
       ROUND((500 + random() * 2500)::numeric, 4)                              AS base_index,
       'OPEN_API'                                                              AS source_type,
       (random() < 0.2)                                                        AS favorite
FROM generate_series(1, 50) AS gs;

-- 2) auto_sync_configs 50개 (DEMO 인덱스에 대해 1:1)
INSERT INTO auto_sync_configs (index_info_id, enabled)
SELECT id, (random() < 0.6)
FROM index_infos
WHERE index_name LIKE 'DEMO-INDEX-%'
ORDER BY id;

-- 3) index_data 50개 (DEMO 인덱스 각각 1행씩)
INSERT INTO index_data
(index_info_id, base_date, source_type, market_price, closing_price, high_price, low_price,
 versus, fluctuation_rate, trading_quantity, trading_price, market_total_amount)
SELECT s.id                                                                                         AS index_info_id,
       DATE '2024-01-01'                                                                            AS base_date,
       'OPEN_API'                                                                                   AS source_type,
       mp.mkt                                                                                       AS market_price,
       mp.cls                                                                                       AS closing_price,
       hl.highp                                                                                     AS high_price,
       hl.lowp                                                                                      AS low_price,
       ROUND((mp.cls - mp.mkt)::numeric, 4)                                                         AS versus,
       ROUND((CASE WHEN mp.mkt = 0 THEN 0 ELSE ((mp.cls - mp.mkt) / mp.mkt) * 100 END)::numeric, 4) AS fluctuation_rate,
       s.qty                                                                                        AS trading_quantity,
       (s.qty::numeric * FLOOR(mp.cls * 100)::numeric)                                              AS trading_price,
       (s.qty::numeric * FLOOR(mp.cls * 1000)::numeric)                                             AS market_total_amount
FROM (SELECT ii.id,
             (FLOOR(random() * 9000000)::bigint + 100000) AS qty
      FROM index_infos ii
      WHERE ii.index_name LIKE 'DEMO-INDEX-%'
      ORDER BY ii.id) s
         CROSS JOIN LATERAL (
    SELECT ROUND((500 + random() * 2500 + (random() * 10 - 5))::numeric, 4) AS mkt,
           ROUND((500 + random() * 2500 + (random() * 10 - 5))::numeric, 4) AS cls
    ) mp
         CROSS JOIN LATERAL (
    SELECT ROUND((GREATEST(mp.mkt, mp.cls) + (random() * 5))::numeric, 4) AS highp,
           ROUND((LEAST(mp.mkt, mp.cls) - (random() * 5))::numeric, 4)    AS lowp
    ) hl;

-- 4) sync_jobs 50개
INSERT INTO sync_jobs
    (index_info_id, job_type, target_date, worker, job_time, result)
SELECT CASE WHEN jt.job_type = 'INDEX_INFO' AND r.rnull < 0.4 THEN NULL ELSE ii.id END AS index_info_id,
       jt.job_type,
       CASE
           WHEN jt.job_type = 'INDEX_DATA' THEN (DATE '2024-01-01' + ((random() * 30)::int))
           ELSE NULL END                                                               AS target_date,
       (ARRAY ['batcher','scheduler','ops','admin'])[(random() * 3)::int + 1]          AS worker,
       NOW() - (random() * (INTERVAL '15 days'))                                       AS job_time,
       CASE WHEN random() < 0.9 THEN 'SUCCESS' ELSE 'FAILED' END                       AS result
FROM (SELECT id
      FROM index_infos
      WHERE index_name LIKE 'DEMO-INDEX-%'
      ORDER BY id
      LIMIT 50) ii
         CROSS JOIN LATERAL (SELECT CASE WHEN random() < 0.5 THEN 'INDEX_INFO' ELSE 'INDEX_DATA' END AS job_type) jt
         CROSS JOIN LATERAL (SELECT random() AS rnull) r;

COMMIT;


SELECT COUNT(*)
FROM index_infos
WHERE index_name LIKE 'DEMO-INDEX-%';
SELECT COUNT(*)
FROM auto_sync_configs a
         JOIN index_infos i ON a.index_info_id = i.id
WHERE i.index_name LIKE 'DEMO-INDEX-%';
SELECT COUNT(*)
FROM index_data d
         JOIN index_infos i ON d.index_info_id = i.id
WHERE i.index_name LIKE 'DEMO-INDEX-%';
SELECT COUNT(*)
FROM sync_jobs;