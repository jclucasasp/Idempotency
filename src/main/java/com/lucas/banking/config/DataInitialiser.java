package com.lucas.banking.config;

import com.lucas.banking.AccountRepository;
import com.lucas.banking.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Log4j2
@RequiredArgsConstructor
public class DataInitialiser implements CommandLineRunner {
    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        Account sourceAccount = new Account("ACC-1000", new BigDecimal("100.00"));
        Account targetAccount = new Account("ACC-2000", new BigDecimal("50.00"));

        accountRepository.saveAllAndFlush(List.of(sourceAccount, targetAccount));

        log.info("Source account ID: {}, account number: {}, initial value: {}", sourceAccount.getId(), sourceAccount.getAccountNumber(), sourceAccount.getBalance());
        log.info("Target account ID: {}, account number: {}, initial value {} ", targetAccount.getId(), targetAccount.getAccountNumber(), targetAccount.getBalance());
    }
}
