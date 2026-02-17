package br.com.mikrotik.features.invoices.model;

import br.com.mikrotik.features.companies.model.Company;
import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.customers.model.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(length = 255)
    private String description;

    @Column(name = "reference_month", nullable = false)
    private LocalDate referenceMonth;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "original_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal originalAmount;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "interest_amount", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal interestAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(name = "payment_link", length = 500)
    private String paymentLink;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = InvoiceStatus.PENDING;
        }
        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }
        if (interestAmount == null) {
            interestAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum InvoiceStatus {
        PENDING,
        PAID,
        PARTIALLY_PAID,
        OVERDUE,
        CANCELED,
        REFUNDED
    }
}
