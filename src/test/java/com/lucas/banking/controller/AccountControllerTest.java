package com.lucas.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.banking.dto.AccountResponse;
import com.lucas.banking.dto.TransferRequest;
import com.lucas.banking.dto.TransferResponse;
import com.lucas.banking.model.IdempotencyStatus;
import com.lucas.banking.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AccountControllerTest {

    private MockMvc mockMvc;

    private PaymentService paymentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        paymentService = Mockito.mock(PaymentService.class);
        AccountController accountController = new AccountController(paymentService);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    void getAccountBalance_ShouldReturnAccount() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountResponse response = new AccountResponse(accountId, "123456", new BigDecimal("100.00"));
        when(paymentService.getAccount(accountId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/account/{accountId}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.accountNumber").value("123456"))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void transfer_ShouldReturnTransferResponse() throws Exception {
        String idempotencyKey = "key";
        TransferRequest request = new TransferRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50.00"));
        TransferResponse response = new TransferResponse(UUID.randomUUID(), IdempotencyStatus.COMPLETED, new BigDecimal("50.00"), "Success");

        when(paymentService.processTransfer(eq(idempotencyKey), any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/account/transfer")
                .header("X-Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void transfer_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        TransferRequest request = new TransferRequest(null, null, new BigDecimal("-50.00"));

        mockMvc.perform(post("/api/v1/account/transfer")
                .header("X-Idempotency-Key", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
