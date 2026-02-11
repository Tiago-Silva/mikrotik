package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(name = "chart_of_account_id", nullable = false)
    private Long chartOfAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "reversed_from_id")
    private Long reversedFromId;


    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", insertable = false, updatable = false)
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chart_of_account_id", insertable = false, updatable = false)
    private ChartOfAccounts chartOfAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
    private Invoice invoice;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = Status.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EntryType {
        CREDIT,   // Entrada de dinheiro (aumenta saldo)
        DEBIT,    // Saída de dinheiro (diminui saldo)
        REVERSAL  // Estorno
    }

    public enum TransactionType {
        INVOICE_PAYMENT,  // Pagamento de Fatura
        MANUAL_ENTRY,     // Lançamento Manual (despesas operacionais)
        TRANSFER,         // Transferência entre contas
        ADJUSTMENT,       // Ajuste de saldo
        REFUND           // Reembolso
    }

    public enum Status {
        ACTIVE,    // Ativo (válido)
        REVERSED,  // Estornado
        CANCELLED  // Cancelado
    }
}

