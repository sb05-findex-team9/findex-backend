-- =========================
-- Drop tables (children -> parents)
-- =========================
DROP TABLE IF EXISTS index_data;
DROP TABLE IF EXISTS sync_jobs;
DROP TABLE IF EXISTS auto_sync_configs;
DROP TABLE IF EXISTS index_infos;

-- =========================
-- index_infos (master)
-- =========================
CREATE TABLE index_infos (
                             id                      bigserial     PRIMARY KEY,
                             index_name              varchar(240)  NOT NULL,
                             index_classification    varchar(240)  NOT NULL,  -- 기존 20 -> 240
                             employed_items_count    int           NOT NULL,
                             base_point_in_time      date          NOT NULL,
                             base_index              numeric(18,4) NOT NULL,
                             source_type             varchar(16)   NOT NULL DEFAULT 'OPEN_API',
                             favorite                boolean       NOT NULL DEFAULT false
);

-- =========================
-- index_data (daily facts)
-- =========================
CREATE TABLE index_data (
                            id                   bigserial     PRIMARY KEY,
                            index_info_id        bigint        NOT NULL,
                            base_date            date          NOT NULL,
                            source_type          varchar(16)   NOT NULL DEFAULT 'OPEN_API',
                            market_price         numeric(18,4) NOT NULL,
                            closing_price        numeric(18,4) NOT NULL,
                            high_price           numeric(18,4) NOT NULL,
                            low_price            numeric(18,4) NOT NULL,
                            versus               numeric(18,4) NOT NULL,
                            fluctuation_rate     numeric(18,4) NOT NULL,
                            trading_quantity     bigint        NOT NULL,
                            trading_price        numeric(21,0) NOT NULL,
                            market_total_amount  numeric(21,0) NOT NULL,
                            CONSTRAINT uq_index_data_info_date UNIQUE (index_info_id, base_date)
);

-- =========================
-- sync_jobs (sync history)
-- =========================
CREATE TABLE sync_jobs (
                           id            bigserial    PRIMARY KEY,
                           index_info_id bigint,                    -- INDEX_INFO 동기화 시 NULL 가능
                           job_type      varchar(16)  NOT NULL,     -- 'INDEX_INFO' | 'INDEX_DATA'
                           target_date   date,                      -- INDEX_INFO 동기화 시 NULL
                           worker        varchar(100),
                           job_time      timestamptz  NOT NULL,
                           result        varchar(16)  NOT NULL,     -- 'SUCCESS' | 'FAILED'
                           CONSTRAINT ck_sync_job_type CHECK (job_type IN ('INDEX_INFO', 'INDEX_DATA')),
                           CONSTRAINT ck_sync_result   CHECK (result IN ('SUCCESS', 'FAILED'))
);

-- =========================
-- auto_sync_configs (per-index switch)
-- =========================
CREATE TABLE auto_sync_configs (
                                   id            bigserial PRIMARY KEY,
                                   index_info_id bigint    NOT NULL,
                                   enabled       boolean   NOT NULL DEFAULT false,
                                   CONSTRAINT uq_auto_sync_index UNIQUE (index_info_id)
);

-- =========================
-- Foreign Keys
-- =========================
ALTER TABLE index_data
    ADD CONSTRAINT fk_index_data__index_infos
        FOREIGN KEY (index_info_id) REFERENCES index_infos (id) ON DELETE CASCADE;

ALTER TABLE sync_jobs
    ADD CONSTRAINT fk_sync_jobs__index_infos
        FOREIGN KEY (index_info_id) REFERENCES index_infos (id) ON DELETE CASCADE;

ALTER TABLE auto_sync_configs
    ADD CONSTRAINT fk_auto_sync_configs__index_infos
        FOREIGN KEY (index_info_id) REFERENCES index_infos (id) ON DELETE CASCADE;

-- =========================
-- Indexes
-- =========================
CREATE INDEX idx_index_data_base_date       ON index_data (base_date);
CREATE INDEX idx_index_data_index_info_id   ON index_data (index_info_id);

CREATE INDEX idx_sync_jobs_job_type         ON sync_jobs (job_type);
CREATE INDEX idx_sync_jobs_job_time         ON sync_jobs (job_time);
CREATE INDEX idx_sync_jobs_target_date      ON sync_jobs (target_date);

CREATE INDEX idx_index_infos_classification ON index_infos (index_classification);
CREATE INDEX idx_index_infos_name           ON index_infos (index_name);
CREATE INDEX idx_index_infos_favorite       ON index_infos (favorite);

-- =========================
-- Sample data (optional)
-- =========================
-- INSERT INTO index_infos
--   (index_name, index_classification, employed_items_count, base_point_in_time, base_index, favorite)
-- VALUES
--   ('IT 서비스', 'KOSPI시리즈', 200, '2000-01-01', 1000.0, true),
--   ('반도체',    'KOSPI시리즈', 150, '2000-01-01', 1000.0, false);
