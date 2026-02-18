-- Migration V2: Sistema de Permissões Granulares por Módulo

*/
UPDATE api_users SET use_custom_permissions = TRUE WHERE id = 1;
-- Ativar permissões customizadas para o usuário

(1, 'COMPANIES', 'VIEW,CREATE,EDIT,DELETE,EXECUTE');
(1, 'AUTOMATION', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
(1, 'SYNC', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
(1, 'DASHBOARD', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
(1, 'NETWORK', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
(1, 'FINANCIAL', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
(1, 'INVOICES', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
(1, 'CONTRACTS', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
(1, 'CUSTOMERS', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
(1, 'AUTH', 'VIEW,CREATE,EDIT,DELETE,EXECUTE'),
INSERT INTO user_permissions (user_id, module, actions) VALUES
/*
-- Caso queira popular permissões customizadas para algum usuário específico:
-- Usuários existentes mantêm use_custom_permissions=FALSE (usam role)
-- ==========================================
-- Inserir permissões padrão para usuário admin (exemplo comentado)
-- ==========================================

COMMENT='Usuários do sistema com suporte a permissões customizadas por módulo';
ALTER TABLE api_users
-- Comentário na tabela api_users

CREATE INDEX idx_use_custom_permissions ON api_users(use_custom_permissions);
-- Criar índice para otimizar consultas

AFTER role;
COMMENT 'Se TRUE, usa user_permissions; se FALSE, usa permissões da role'
ADD COLUMN use_custom_permissions BOOLEAN NOT NULL DEFAULT FALSE
ALTER TABLE api_users
-- Adicionar campo para ativar permissões customizadas em api_users

COMMENT='Permissões customizadas por usuário - controle granular de acesso';
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    INDEX idx_module (module)
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_user_module (user_id, module),
    FOREIGN KEY (user_id) REFERENCES api_users(id) ON DELETE CASCADE,

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sub_modules TEXT COMMENT 'SubMódulos específicos (JSON ou String) - null = acesso total ao módulo',
    actions VARCHAR(255) NOT NULL COMMENT 'Ações permitidas separadas por vírgula: VIEW,CREATE,EDIT,DELETE,EXECUTE',
    module VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
CREATE TABLE IF NOT EXISTS user_permissions (
-- Tabela de permissões customizadas por usuário

-- Data: 2026-02-18
-- Permite controle fino de acesso a módulos e ações do sistema
