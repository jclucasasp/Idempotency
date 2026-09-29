package com.lucas.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
@Schema(name = "AccountResponse", description = "The account response object.")
public record AccountResponse(
        @Schema(description = "The uuid of the account")
        UUID accountId,
        @Schema(description = "The account number")
        String accountNumber,
        @Schema(description = "The account balance")
        BigDecimal amount
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
