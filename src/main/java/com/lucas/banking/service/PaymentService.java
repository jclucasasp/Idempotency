package com.lucas.banking.service;

import com.lucas.banking.AccountRepository;
import com.lucas.banking.dto.AccountResponse;
import com.lucas.banking.dto.TransferRequest;
import com.lucas.banking.dto.TransferResponse;
import com.lucas.banking.model.Account;
import com.lucas.banking.model.IdempotencyRecord;
import com.lucas.banking.model.IdempotencyStatus;
import com.lucas.banking.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final AccountRepository accountRepository;
    private final IdempotencyRepository idempotencyRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public TransferResponse processTransfer(String idempotencyKey, TransferRequest transferRequest) throws AccountNotFoundException {
           // Idempotency check
        Optional<IdempotencyRecord> existingRecord = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();
            if (record.getStatus() == IdempotencyStatus.COMPLETED) {
                return new TransferResponse(record.getTransferId(), record.getStatus(), record.getAmount(), "Transfer already completed");
            } else if (record.getStatus() == IdempotencyStatus.PROCESSING) {
                throw new ConcurrencyFailureException("A transaction with this idempotency key is currently being progressed");
            }
        }

        //Save processing state
        IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, IdempotencyStatus.PROCESSING, Instant.now());

        // Prevent Deadlocks: Lock accounts in a deterministic order (lower UUID first)
        UUID sourceId = transferRequest.getSourceAccountId();
        UUID targetId = transferRequest.getTargetAccountId();
        // Edge case
        if (sourceId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account.");
        }

        Account sourceAccount;
        Account targetAccount;

        // Creates the physical locks by calling the findByIdWithLock method
        if (sourceId.compareTo(targetId) < 0) {
            sourceAccount = accountRepository.findByIdWithLock(sourceId)
                    .orElseThrow(() -> new AccountNotFoundException("Source account not found" + sourceId));
            targetAccount = accountRepository.findByIdWithLock(targetId)
                    .orElseThrow(() -> new AccountNotFoundException("Target account not found" + targetId));
        } else {
            targetAccount = accountRepository.findByIdWithLock(targetId)
                    .orElseThrow(() -> new AccountNotFoundException("Target account not found" + targetId));
            sourceAccount = accountRepository.findByIdWithLock(sourceId)
                    .orElseThrow(() -> new AccountNotFoundException("Source account not found" + sourceId));
        }

        // Check balance
        BigDecimal amount = transferRequest.getAmount();
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            record.setStatus(IdempotencyStatus.FAILED);
            idempotencyRepository.saveAndFlush(record);
            // Can create a custom exception
            throw new IllegalArgumentException("Insufficient funds");
        }

        // Perform transfer
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        targetAccount.setBalance(targetAccount.getBalance().add(amount));

        // Save transfer
        accountRepository.saveAllAndFlush(List.of(sourceAccount, targetAccount));

        //Update Idempotency Record
        UUID transferId = UUID.randomUUID();
        record.setTransferId(transferId);
        record.setAmount(amount);
        record.setStatus(IdempotencyStatus.COMPLETED);
        record.setMessage("Transfer completed successfully");
        idempotencyRepository.saveAndFlush(record);

        return new TransferResponse(transferId, record.getStatus(), record.getAmount(), record.getMessage());
    }

    public AccountResponse getAccount(UUID id) {
        Account account = accountRepository.getReferenceById(id);
        return new AccountResponse(account.getId(), account.getAccountNumber(), account.getBalance());
    }

}
