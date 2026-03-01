package br.com.mikrotik.features.invoices.service;

import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.invoices.event.InvoiceCreatedEvent;
import br.com.mikrotik.features.invoices.model.Invoice;
import br.com.mikrotik.features.invoices.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Responsável exclusivamente pela criação transacional de uma única fatura.
 *
 * MOTIVAÇÃO (Auto-invocação / Self-invocation):
 *  BillingService chama createInvoiceIfAbsent() em um loop. Se ambos estivessem na
 *  mesma classe, o @Transactional seria ignorado (self-invocation — Spring AOP não
 *  intercepta chamadas internas). Ao extrair para este bean separado, cada fatura
 *  tem sua própria transação garantida pelo proxy do Spring, isolando falhas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingInvoiceCreator {

    private final InvoiceRepository invoiceRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("MM/yyyy");

    /**
     * Persiste uma fatura para o contrato no mês de referência, se ainda não existir.
     *
     * @Transactional: cada contrato tem sua própria transação.
     *   Falha em um não reverte os demais já criados pelo loop no BillingService.
     *
     * @return true se a fatura foi criada, false se já existia (idempotência).
     */
    @Transactional
    public boolean createIfAbsent(Contract contract, LocalDate referenceMonth, LocalDate today) {

        // ─── IDEMPOTÊNCIA ────────────────────────────────────────────────────────
        if (invoiceRepository.existsByContractIdAndReferenceMonth(contract.getId(), referenceMonth)) {
            log.debug("Fatura já existe para contrato #{} no mês {}. Ignorando.",
                    contract.getId(), referenceMonth);
            return false;
        }

        // ─── CÁLCULO DE VENCIMENTO ───────────────────────────────────────────────
        LocalDate dueDate = today.withDayOfMonth(contract.getBillingDay());
        if (dueDate.isBefore(today)) {
            dueDate = dueDate.plusMonths(1);
        }

        // ─── DESCRIÇÃO ──────────────────────────────────────────────────────────
        String monthYear = referenceMonth.format(MONTH_FORMATTER);
        String description = "Mensalidade Internet - " + monthYear;

        // ─── PERSISTÊNCIA (SEM CHAMADA EXTERNA AQUI) ────────────────────────────
        Invoice invoice = Invoice.builder()
                .companyId(contract.getCompanyId())
                .contractId(contract.getId())
                .customerId(contract.getCustomerId())
                .description(description)
                .referenceMonth(referenceMonth)
                .dueDate(dueDate)
                .originalAmount(contract.getAmount())
                .discountAmount(BigDecimal.ZERO)
                .interestAmount(BigDecimal.ZERO)
                .finalAmount(contract.getAmount())
                .status(Invoice.InvoiceStatus.PENDING)
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        log.info("Fatura #{} criada — contrato #{} cliente #{} valor=R${} vencimento={}",
                saved.getId(), contract.getId(), contract.getCustomerId(),
                saved.getFinalAmount(), dueDate);

        // ─── EVENTO PUBLICADO APÓS COMMIT (AFTER_COMMIT via TransactionalEventListener) ──
        // AbacatePayEventListener consome assincronamente e grava o link PIX.
        // A chamada ao gateway NUNCA ocorre dentro desta transação.
        eventPublisher.publishEvent(new InvoiceCreatedEvent(saved.getId(), saved.getCompanyId()));

        return true;
    }
}

