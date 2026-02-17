package br.com.mikrotik.features.financial.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chart_of_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartOfAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "parent_id")
    private Long parentId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AccountType {
        REVENUE,    // Receita
        EXPENSE,    // Despesa
        ASSET,      // Ativo
        LIABILITY,  // Passivo
        EQUITY      // Patrimônio Líquido
    }

    public enum Category {
        // Receitas
        SUBSCRIPTION_REVENUE, // Receita de Assinaturas/Mensalidades
        INSTALLATION_FEE,     // Taxa de Instalação
        LATE_FEE,            // Multas e Juros
        OTHER_REVENUE,       // Outras Receitas

        // Despesas
        LINK_COST,           // Custo de Links/Internet
        SALARY,              // Salários e Encargos
        RENT,                // Aluguel
        MARKETING,           // Marketing e Publicidade
        MAINTENANCE,         // Manutenção
        TAX,                 // Impostos
        OTHER_EXPENSE,       // Outras Despesas

        // Ativos
        CASH,                // Caixa
        BANK,                // Banco
        ACCOUNTS_RECEIVABLE, // Contas a Receber

        // Passivos
        ACCOUNTS_PAYABLE,    // Contas a Pagar
        LOAN,                // Empréstimos

        // Patrimônio
        CAPITAL              // Capital Social
    }
}

