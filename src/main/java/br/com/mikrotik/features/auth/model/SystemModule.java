package br.com.mikrotik.features.auth.model;

import lombok.Getter;

/**
 * Módulos do sistema disponíveis para controle de acesso granular
 * Cada módulo representa uma feature funcional do ISP
 */
@Getter
public enum SystemModule {

    // Módulos principais
    AUTH("Autenticação", "Gerenciamento de usuários e autenticação", "auth"),
    CUSTOMERS("Clientes", "Cadastro e gestão de clientes", "customers"),
    CONTRACTS("Contratos", "Contratos e planos de serviço", "contracts"),
    INVOICES("Faturas", "Faturamento e cobranças", "invoices"),
    FINANCIAL("Financeiro", "Fluxo de caixa e transações", "financial"),
    NETWORK("Rede", "Infraestrutura Mikrotik e PPPoE", "network"),
    DASHBOARD("Dashboard", "Painéis e estatísticas", "dashboard"),
    SYNC("Sincronização", "Sincronização com Mikrotik", "sync"),
    AUTOMATION("Automação", "Logs e automações do sistema", "automation"),
    COMPANIES("Empresas", "Gestão multi-tenant", "companies");

    private final String displayName;
    private final String description;
    private final String path; // Prefixo da rota (ex: /api/customers)

    SystemModule(String displayName, String description, String path) {
        this.displayName = displayName;
        this.description = description;
        this.path = path;
    }

    /**
     * Verifica se o módulo requer acesso administrativo por padrão
     */
    public boolean isAdminOnly() {
        return this == COMPANIES || this == SYNC;
    }

    /**
     * Verifica se o módulo é crítico para operação do sistema
     */
    public boolean isCritical() {
        return this == AUTH || this == CUSTOMERS || this == CONTRACTS;
    }

    /**
     * Retorna módulos disponíveis para uma role específica (permissões padrão)
     */
    public static SystemModule[] getDefaultModulesForRole(UserRole role) {
        return switch (role) {
            case ADMIN -> values(); // Todos os módulos
            case OPERATOR -> new SystemModule[]{
                CUSTOMERS, CONTRACTS, INVOICES, NETWORK, DASHBOARD, AUTOMATION
            };
            case FINANCIAL -> new SystemModule[]{
                CUSTOMERS, CONTRACTS, INVOICES, FINANCIAL, DASHBOARD
            };
            case TECHNICAL -> new SystemModule[]{
                CUSTOMERS, CONTRACTS, NETWORK, DASHBOARD
            };
            case VIEWER -> new SystemModule[]{
                CUSTOMERS, CONTRACTS, INVOICES, DASHBOARD
            };
        };
    }
}

