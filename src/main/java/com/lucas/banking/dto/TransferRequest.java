package com.lucas.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(name = "TransferRequest", description = "Payload for transferring money from one account to another")
public class TransferRequest {
    @NotNull(message = "Source account ID is required")
    @Schema(description = "The source account id")
    private UUID sourceAccountId;

    @NotNull(message = "Destination account ID is required")
    @Schema(description = "The target account id")
    private UUID targetAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater then zero.")
    @Schema(description = "The amount to be transferred")
    private BigDecimal amount;
}
