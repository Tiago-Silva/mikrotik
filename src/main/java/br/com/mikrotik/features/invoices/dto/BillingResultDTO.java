package br.com.mikrotik.features.invoices.dto;

import java.util.List;

/**
 * Resultado imutável da operação de geração de faturas mensais em lote.
 *
 * @param companyId       Empresa processada
 * @param referenceMonth  Mês de referência (ex: "2026-03")
 * @param created         Número de faturas criadas com sucesso
 * @param skipped         Número de contratos ignorados por duplicidade (fatura já existia)
 * @param errors          Número de contratos que falharam
 * @param errorDetails    Lista descritiva dos erros ocorridos (para auditoria/debug)
 */
public record BillingResultDTO(
        Long companyId,
        String referenceMonth,
        int created,
        int skipped,
        int errors,
        List<String> errorDetails
) {}

