-- V3: Tabela de auditoria para geração de faturas em lote
-- Registra cada execução (manual via endpoint ou automática via Job)
-- para fins de compliance e troubleshooting operacional.

CREATE TABLE IF NOT EXISTS billing_generation_log (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id       BIGINT       NOT NULL,
    reference_month  DATE         NOT NULL COMMENT 'Primeiro dia do mês de referência (ex: 2026-03-01)',
    triggered_by     VARCHAR(20)  NOT NULL COMMENT 'JOB ou MANUAL',
    created_count    INT          NOT NULL DEFAULT 0,
    skipped_count    INT          NOT NULL DEFAULT 0,
    error_count      INT          NOT NULL DEFAULT 0,
    error_details    TEXT         NULL     COMMENT 'JSON array com detalhes dos erros',
    executed_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_blg_company_month (company_id, reference_month),
    INDEX idx_blg_executed_at   (executed_at)
) COMMENT = 'Auditoria imutável de cada ciclo de geração de faturas mensais';

