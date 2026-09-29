package com.lucas.banking.controller;

import com.lucas.banking.dto.AccountResponse;
import com.lucas.banking.dto.TransferRequest;
import com.lucas.banking.dto.TransferResponse;
import com.lucas.banking.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.security.auth.login.AccountNotFoundException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account")
@Tag(name = "01. Account", description = "Account endpoint")
@RequiredArgsConstructor
public class AccountController {
    private final PaymentService paymentService;

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Account balance enquiry", description = "Query account balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Query successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request",
            content =  @Content(schema = @Schema(implementation = HttpClientErrorException.BadRequest.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorised",
            content =  @Content(schema = @Schema(implementation = HttpClientErrorException.Unauthorized.class))),
            @ApiResponse(responseCode = "404", description = "Query unsuccessful"),
            @ApiResponse(responseCode = "500", description = "Internal server error" ,
            content = @Content(schema = @Schema(implementation = HttpServerErrorException.InternalServerError.class)))
    })
    public ResponseEntity<AccountResponse> getAccountBalance(@PathVariable UUID accountId) {
        return ResponseEntity.ok(paymentService.getAccount(accountId));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Account transfer endpoint", description = "Returns the balance of an account after a transfer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content =  @Content(schema = @Schema(implementation = HttpClientErrorException.BadRequest.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorised",
                    content =  @Content(schema = @Schema(implementation = HttpClientErrorException.Unauthorized.class))),
            @ApiResponse(responseCode = "404", description = "Transfer unsuccessful"),
            @ApiResponse(responseCode = "500", description = "Internal server error" ,
                    content = @Content(schema = @Schema(implementation = HttpServerErrorException.InternalServerError.class)))
    })
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("X-Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody TransferRequest transferRequest
    ) throws AccountNotFoundException {
        return ResponseEntity.ok(paymentService.processTransfer(idempotencyKey, transferRequest));
    }
}
