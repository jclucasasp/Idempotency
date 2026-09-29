package com.lucas.banking.dto;

import com.lucas.banking.model.IdempotencyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;


@Schema(name = "TransferResponse", description = "The object returned from doing a transfer")
public record TransferResponse(
        @Schema(description = "Unique identifier of the transfer")
        UUID transferId,
        @Schema(description = "Idempotency status")
        IdempotencyStatus status,
        @Schema(description = "Amount to be transferred")
        BigDecimal amount,
        @Schema(description = "Additional messages of the transfer")
        String message
) implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;
}
