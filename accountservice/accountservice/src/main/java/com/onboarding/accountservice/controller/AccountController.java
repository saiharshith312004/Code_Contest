package com.onboarding.accountservice.controller;

import com.onboarding.accountservice.model.Account;
import com.onboarding.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;

    /**
     * Get all accounts
     * @return List of all accounts
     */
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.findAllAccounts());
    }

    /**
     * Get account by customer ID
     * @param customerId the customer ID
     * @return Account details if found, 404 otherwise
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Account> getAccountByCustomerId(@PathVariable Long customerId) {
        return accountService.findByCustomerId(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get account by account number
     * @param accountNumber the account number
     * @return Account details if found, 404 otherwise
     */
    @GetMapping("/account-number/{accountNumber}")
    public ResponseEntity<Account> getAccountByAccountNumber(@PathVariable String accountNumber) {
        return accountService.findByAccountNumber(accountNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}




