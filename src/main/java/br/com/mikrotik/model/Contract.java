package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "service_plan_id", nullable = false)
    private Long servicePlanId;

    @Column(name = "pppoe_credential_id", unique = true)
    private Long pppoeCredentialId;

    @Column(name = "installation_address_id")
    private Long installationAddressId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "billing_day", nullable = false)
    @Builder.Default
    private Integer billingDay = 10;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_plan_id", insertable = false, updatable = false)
    private ServicePlan servicePlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pppoe_credential_id", insertable = false, updatable = false)
    private PppoeCredential pppoeCredential;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installation_address_id", insertable = false, updatable = false)
    private Address installationAddress;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ContractStatus.DRAFT;
        }
        if (billingDay == null) {
            billingDay = 10;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ContractStatus {
        DRAFT,
        ACTIVE,
        SUSPENDED_FINANCIAL,
        SUSPENDED_REQUEST,
        CANCELED
    }
}
