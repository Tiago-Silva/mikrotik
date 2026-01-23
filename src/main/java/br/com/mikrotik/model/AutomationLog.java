package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "automation_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "contract_id")
    private Long contractId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 20)
    private ActionType actionType;

    @Column(length = 255)
    private String reason;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    private Boolean success;

    @Column(name = "output_message", columnDefinition = "TEXT")
    private String outputMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", insertable = false, updatable = false)
    private Contract contract;

    @PrePersist
    protected void onCreate() {
        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
    }

    public enum ActionType {
        BLOCK,
        UNBLOCK,
        REDUCE_SPEED,
        SEND_WARNING,
        SEND_EMAIL,
        SEND_SMS
    }
}
