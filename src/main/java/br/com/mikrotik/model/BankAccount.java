package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "bank_code", length = 10)
    private String bankCode;

    @Column(length = 20)
    private String agency;

    @Column(name = "account_number", length = 30)
    private String accountNumber;

    @Builder.Default
    @Column(name = "initial_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (initialBalance == null) {
            initialBalance = BigDecimal.ZERO;
        }
        if (currentBalance == null) {
            currentBalance = initialBalance;
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza o saldo da conta de forma thread-safe (usar com PESSIMISTIC_WRITE lock)
     */
    public void updateBalance(BigDecimal amount, FinancialEntry.EntryType entryType) {
        if (entryType == FinancialEntry.EntryType.CREDIT) {
            this.currentBalance = this.currentBalance.add(amount);
        } else if (entryType == FinancialEntry.EntryType.DEBIT) {
            this.currentBalance = this.currentBalance.subtract(amount);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public enum AccountType {
        CHECKING,       // Conta Corrente
        SAVINGS,        // Poupança
        CASH,           // Caixa Geral
        CASH_INTERNAL,  // Caixa Interno (uso da empresa)
        DIGITAL_WALLET, // Carteira Digital (PicPay, Mercado Pago, etc)
        CREDIT_CARD     // Cartão de Crédito
    }
}
