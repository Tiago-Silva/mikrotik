-- Migration: Initial Schema with Multi-tenant Support
-- Description: Cria todas as tabelas do sistema desde o início com suporte multi-tenant
-- Author: Development Team
-- Date: 2026-01-22

SET FOREIGN_KEY_CHECKS = 0;
SET time_zone = '-03:00';

-- ==========================================
-- 1. CAMADA CORPORATIVA (Multi-tenant)
-- ==========================================

CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    email VARCHAR(255),
    support_phone VARCHAR(20),
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cnpj (cnpj),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Usuários da API (com suporte multi-tenant)
CREATE TABLE api_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,  -- NOT NULL: todo usuário DEVE pertencer a uma empresa
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login DATETIME,
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 2. CAMADA DE CLIENTES (CRM)
-- ==========================================

CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type ENUM('FISICA', 'JURIDICA') NOT NULL,
    document VARCHAR(20) NOT NULL,
    rg_ie VARCHAR(20),
    email VARCHAR(255),
    phone_primary VARCHAR(20),
    phone_whatsapp VARCHAR(20),
    status ENUM('ACTIVE', 'SUSPENDED', 'CANCELED', 'PROSPECT') DEFAULT 'PROSPECT',
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    UNIQUE KEY uk_doc_company (document, company_id),
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    type ENUM('BILLING', 'INSTALLATION', 'BOTH') DEFAULT 'BOTH',
    zip_code VARCHAR(10),
    street VARCHAR(255),
    number VARCHAR(20),
    complement VARCHAR(255),
    district VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(2),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    INDEX idx_customer_id (customer_id),
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 3. CAMADA TÉCNICA (Mikrotik / Rede)
-- ==========================================

-- Tabela de Servidores Mikrotik
CREATE TABLE mikrotik_servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    api_port INT DEFAULT 8728,
    ssh_port INT DEFAULT 22,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    last_sync_at DATETIME,
    sync_status ENUM('OK', 'ERROR', 'UNREACHABLE'),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    UNIQUE KEY uk_name_company (name, company_id),
    INDEX idx_name (name),
    INDEX idx_ip_address (ip_address),
    INDEX idx_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Pools de IP
CREATE TABLE ip_pools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mikrotik_server_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    cidr VARCHAR(20),
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mikrotik_server_id) REFERENCES mikrotik_servers(id) ON DELETE CASCADE,
    INDEX idx_mikrotik_server_id (mikrotik_server_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Perfis PPPoE (sincronizados do Mikrotik)
CREATE TABLE pppoe_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    max_bitrate_dl BIGINT NOT NULL,
    max_bitrate_ul BIGINT NOT NULL,
    session_timeout INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    mikrotik_server_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (mikrotik_server_id) REFERENCES mikrotik_servers(id) ON DELETE CASCADE,
    INDEX idx_name (name),
    INDEX idx_mikrotik_server_id (mikrotik_server_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Credenciais PPPoE
CREATE TABLE pppoe_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    mikrotik_server_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    mac_address VARCHAR(17),
    static_ip VARCHAR(45),
    status ENUM('ONLINE', 'OFFLINE', 'DISABLED') DEFAULT 'OFFLINE',
    last_connection_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (mikrotik_server_id) REFERENCES mikrotik_servers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_server (username, mikrotik_server_id),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela antiga de Usuários PPPoE (compatibilidade)
CREATE TABLE pppoe_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    comment TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    pppoe_profile_id BIGINT NOT NULL,
    mikrotik_server_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pppoe_profile_id) REFERENCES pppoe_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (mikrotik_server_id) REFERENCES mikrotik_servers(id) ON DELETE CASCADE,
    INDEX idx_username (username),
    INDEX idx_pppoe_profile_id (pppoe_profile_id),
    INDEX idx_mikrotik_server_id (mikrotik_server_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4. CAMADA COMERCIAL & CONTRATOS
-- ==========================================

-- Planos Comerciais
CREATE TABLE service_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(19, 2) NOT NULL,
    pppoe_profile_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (pppoe_profile_id) REFERENCES pppoe_profiles(id) ON DELETE RESTRICT,
    INDEX idx_company_id (company_id),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Contratos
CREATE TABLE contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    service_plan_id BIGINT NOT NULL,
    pppoe_credential_id BIGINT UNIQUE,
    installation_address_id BIGINT,
    status ENUM('DRAFT', 'ACTIVE', 'SUSPENDED_FINANCIAL', 'SUSPENDED_REQUEST', 'CANCELED') DEFAULT 'DRAFT',
    billing_day INT NOT NULL DEFAULT 10,
    amount DECIMAL(19, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    cancellation_date DATE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    FOREIGN KEY (service_plan_id) REFERENCES service_plans(id) ON DELETE RESTRICT,
    FOREIGN KEY (pppoe_credential_id) REFERENCES pppoe_credentials(id) ON DELETE SET NULL,
    FOREIGN KEY (installation_address_id) REFERENCES addresses(id) ON DELETE SET NULL,
    INDEX idx_company_id (company_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 5. CAMADA FINANCEIRA
-- ==========================================

CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    description VARCHAR(255),
    reference_month DATE NOT NULL,
    due_date DATE NOT NULL,
    original_amount DECIMAL(19, 2) NOT NULL,
    discount_amount DECIMAL(19, 2) DEFAULT 0.00,
    interest_amount DECIMAL(19, 2) DEFAULT 0.00,
    final_amount DECIMAL(19, 2) NOT NULL,
    status ENUM('PENDING', 'PAID', 'PARTIALLY_PAID', 'OVERDUE', 'CANCELED', 'REFUNDED') DEFAULT 'PENDING',
    payment_link VARCHAR(500),
    external_id VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    INDEX idx_company_id (company_id),
    INDEX idx_status_due (status, due_date),
    INDEX idx_customer_id (customer_id),
    INDEX idx_reference_month (reference_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    amount_paid DECIMAL(19, 2) NOT NULL,
    paid_at DATETIME NOT NULL,
    method ENUM('BOLETO', 'PIX', 'CREDIT_CARD', 'CASH', 'TRANSFER') NOT NULL,
    transaction_code VARCHAR(255),
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE RESTRICT,
    INDEX idx_invoice_id (invoice_id),
    INDEX idx_paid_at (paid_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 6. AUTOMAÇÃO E AUDITORIA
-- ==========================================

CREATE TABLE automation_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    contract_id BIGINT,
    action_type ENUM('BLOCK', 'UNBLOCK', 'REDUCE_SPEED', 'SEND_WARNING', 'SEND_EMAIL', 'SEND_SMS'),
    reason VARCHAR(255),
    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN,
    output_message TEXT,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE SET NULL,
    INDEX idx_company_id (company_id),
    INDEX idx_executed_at (executed_at),
    INDEX idx_action_type (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Conexões PPPoE
CREATE TABLE pppoe_connections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pppoe_user_id BIGINT NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    calling_station_id VARCHAR(255) NOT NULL,
    connected_at DATETIME NOT NULL,
    disconnected_at DATETIME,
    bytes_up BIGINT NOT NULL DEFAULT 0,
    bytes_down BIGINT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    mikrotik_server_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pppoe_user_id) REFERENCES pppoe_users(id) ON DELETE CASCADE,
    FOREIGN KEY (mikrotik_server_id) REFERENCES mikrotik_servers(id) ON DELETE CASCADE,
    INDEX idx_pppoe_user_id (pppoe_user_id),
    INDEX idx_mikrotik_server_id (mikrotik_server_id),
    INDEX idx_connected_at (connected_at),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Log de Auditoria
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    user_id BIGINT,
    entity_type VARCHAR(50),
    entity_id VARCHAR(50),
    action VARCHAR(50),
    old_value JSON,
    new_value JSON,
    ip_address VARCHAR(45),
    details TEXT,
    performed_by VARCHAR(255) NOT NULL,
    mikrotik_server_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (user_id) REFERENCES api_users(id) ON DELETE SET NULL,
    FOREIGN KEY (mikrotik_server_id) REFERENCES mikrotik_servers(id) ON DELETE SET NULL,
    INDEX idx_company_id (company_id),
    INDEX idx_action (action),
    INDEX idx_entity (entity_type),
    INDEX idx_performed_by (performed_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reabilitar verificação de foreign keys
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- DADOS INICIAIS
-- ==========================================
-- Os dados iniciais (empresa e usuários) são criados pela classe:
-- br.com.mikrotik.config.DataInitializationConfig
--
-- Isso garante que:
-- 1. A empresa é criada ANTES dos usuários
-- 2. Os usuários são corretamente vinculados à empresa
-- 3. As senhas são criptografadas com BCrypt
--
-- Migration V1 - Schema com 18 tabelas criado com sucesso!
-- ==========================================
