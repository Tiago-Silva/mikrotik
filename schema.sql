-- Script para criar banco de dados e tabelas
-- Mikrotik PPPoE Management API

-- Criar banco de dados
CREATE DATABASE IF NOT EXISTS mikrotik_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mikrotik_db;

-- Tabela de Usuários da API
CREATE TABLE IF NOT EXISTS api_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Servidores Mikrotik
CREATE TABLE IF NOT EXISTS mikrotik_servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(255) NOT NULL UNIQUE,
    port INT NOT NULL DEFAULT 22,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Perfis PPPoE
CREATE TABLE IF NOT EXISTS pppoe_profiles (
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

-- Tabela de Usuários PPPoE
CREATE TABLE IF NOT EXISTS pppoe_users (
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

-- Tabela de Conexões PPPoE
CREATE TABLE IF NOT EXISTS pppoe_connections (
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
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    entity VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    details TEXT,
    performed_by VARCHAR(255) NOT NULL,
    mikrotik_server_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mikrotik_server_id) REFERENCES mikrotik_servers(id) ON DELETE SET NULL,
    INDEX idx_action (action),
    INDEX idx_entity (entity),
    INDEX idx_performed_by (performed_by),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inserir usuários padrão (opcional - comentar se usar DataInitializationConfig)
-- INSERT INTO api_users (username, password, email, role, active) VALUES
-- ('admin', '$2a$10$SlVZrKU8T8zXzYVhfLN.gO.8.K9YPqKyHbPvvQx9K5LVo1k2B5hKe', 'admin@example.com', 'ADMIN', true),
-- ('operator', '$2a$10$SlVZrKU8T8zXzYVhfLN.gO.8.K9YPqKyHbPvvQx9K5LVo1k2B5hKe', 'operator@example.com', 'OPERATOR', true),
-- ('viewer', '$2a$10$SlVZrKU8T8zXzYVhfLN.gO.8.K9YPqKyHbPvvQx9K5LVo1k2B5hKe', 'viewer@example.com', 'VIEWER', true);
