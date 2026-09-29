package com.lucas.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal balance;

    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

}
