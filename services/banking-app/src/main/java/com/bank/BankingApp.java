package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single entry point of the modular monolith.
 * Phase 7 split will move identity/transfer/... into own deployables;
 * until then they live as strict packages under com.bank.*.
 */
@SpringBootApplication
public class BankingApp {

    public static void main(String[] args) {
        SpringApplication.run(BankingApp.class, args);
    }
}