package br.com.mikrotik.features.financial.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(name = "balance_date", nullable = false)
    private LocalDate balanceDate;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "total_credits", precision = 19, scale = 2)
    private BigDecimal totalCredits = BigDecimal.ZERO;

    @Column(name = "total_debits", precision = 19, scale = 2)
    private BigDecimal totalDebits = BigDecimal.ZERO;

    @Column(name = "closing_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", insertable = false, updatable = false)
    private BankAccount bankAccount;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (totalCredits == null) {
            totalCredits = BigDecimal.ZERO;
        }
        if (totalDebits == null) {
            totalDebits = BigDecimal.ZERO;
        }
        // Calcular saldo de fechamento
        if (closingBalance == null) {
            closingBalance = openingBalance.add(totalCredits).subtract(totalDebits);
        }
    }
}

