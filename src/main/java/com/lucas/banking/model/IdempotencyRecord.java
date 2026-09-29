package com.lucas.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "idempotency_records")
public class IdempotencyRecord {
    @Id
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    private UUID transferId;
    private BigDecimal amount;
    private String message;
    private Instant createdAt;

    public IdempotencyRecord(String idempotencyKey, IdempotencyStatus idempotencyStatus, Instant now) {
        this.idempotencyKey = idempotencyKey;
        this.status = idempotencyStatus;
        this.createdAt = now;
    }
}
