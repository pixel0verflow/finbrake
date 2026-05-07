package net.bartcloud.finbrake.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "load_attempts",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_load_attempt",
                        columnNames = {"customer_id", "attempt_id"}),
        indexes = {@Index(name = "ix_customer_attempt_time", columnList = "customer_id, attempt_time")})
public class LoadAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(name = "attempt_id", nullable = false, length = 64)
    private String attemptId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Column(name = "attempt_time", nullable = false)
    private Instant attemptTime;

    @Column(name = "accepted", nullable = false)
    private boolean accepted;

    protected LoadAttemptEntity() {}

    public LoadAttemptEntity(
            String attemptId, String customerId, BigDecimal amount, Instant attemptTime, boolean accepted) {
        this.attemptId = attemptId;
        this.customerId = customerId;
        this.amount = amount;
        this.attemptTime = attemptTime;
        this.accepted = accepted;
    }

    public Long getPk() {
        return pk;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getAttemptTime() {
        return attemptTime;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
