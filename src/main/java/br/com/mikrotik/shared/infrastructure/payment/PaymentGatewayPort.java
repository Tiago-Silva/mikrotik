package br.com.mikrotik.shared.infrastructure.payment;

import br.com.mikrotik.features.invoices.model.Invoice;

import java.util.Optional;

/**
 * Porta (Port) de saída para geração de cobranças PIX.
 *
 * Hexagonal / Ports & Adapters:
 *  - Esta interface pertence ao domínio/aplicação.
 *  - Implementações concretas (AbacatePay, Asaas, etc.) são Adapters.
 *  - O BillingService depende desta abstração, nunca da implementação direta.
 *
 * Regra de Ouro (NON-NEGOTIABLE):
 *  - Nunca chamar este método dentro de um @Transactional aberto.
 *  - O BillingService salva a fatura, encerra a transação e então publica
 *    o InvoiceCreatedEvent. O listener chama este método fora da transação.
 */
public interface PaymentGatewayPort {

    /**
     * Gera uma cobrança PIX para a fatura informada.
     *
     * @param invoice  Entidade persistida (já tem ID e valor definidos)
     * @return         Optional vazio se o gateway estiver indisponível (fail-safe)
     */
    Optional<PixChargeResult> generatePixCharge(Invoice invoice);
}

