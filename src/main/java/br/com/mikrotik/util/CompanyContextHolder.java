package br.com.mikrotik.util;

/**
 * Classe utilitária para armazenar o ID da empresa (company_id) do usuário logado
 * usando ThreadLocal. Isso permite que qualquer parte da aplicação acesse o contexto
 * da empresa sem precisar passar o parâmetro explicitamente.
 */
public class CompanyContextHolder {

    private static final ThreadLocal<Long> companyIdHolder = new ThreadLocal<>();

    /**
     * Define o ID da empresa no contexto da thread atual
     */
    public static void setCompanyId(Long companyId) {
        companyIdHolder.set(companyId);
    }

    /**
     * Obtém o ID da empresa do contexto da thread atual
     */
    public static Long getCompanyId() {
        return companyIdHolder.get();
    }

    /**
     * Limpa o contexto da thread atual (importante para evitar memory leaks)
     */
    public static void clear() {
        companyIdHolder.remove();
    }
}
