package com.onboarding.accountservice.service;

import com.onboarding.accountservice.dto.CustomerDTO;
import com.onboarding.accountservice.model.Account;
import com.onboarding.accountservice.model.AccountStatus;
import com.onboarding.accountservice.model.AccountType;
import com.onboarding.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    
    private final AccountRepository accountRepository;
    private final AuthServiceClient authServiceClient;
    // Rely on Account entity's @PrePersist to generate account numbers

    @Override
    @Transactional
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void createAccountForCustomer(Long customerId) {
        log.info("Starting account creation for customer: {}", customerId);
        
        // 1. Check if account already exists
        if (accountRepository.existsByCustomerId(customerId)) {
            log.info("Account already exists for customer: {}", customerId);
            return;
        }
        
        try {
            // 2. Create and save account with default values
            Account account = new Account();
            account.setCustomerId(customerId);
            account.setAccountType(AccountType.SAVINGS);
            account.setStatus(AccountStatus.ACTIVE);
            account.setAccountHolderName("Customer-" + customerId); // Default account name
            
            // 3. Try to get customer details from Auth Service (if available)
            try {
                log.debug("Attempting to fetch customer details for: {}", customerId);
                CustomerDTO customer = authServiceClient.getCustomerById(customerId);
                if (customer != null) {
                    String accountName = customer.getFirstName();
                    if (customer.getLastName() != null && !customer.getLastName().isEmpty()) {
                        accountName += " " + customer.getLastName();
                    }
                    account.setAccountHolderName(accountName);
                    log.debug("Updated account holder name to: {}", accountName);
                }
            } catch (Exception e) {
                log.warn("Could not fetch customer details for {}: {}. Using default account name.", 
                        customerId, e.getMessage());
                // Continue with default account name
            }
            
            accountRepository.save(account);
            log.info("Successfully created account {} for customer {}", 
                    account.getAccountNumber(), customerId);
                    
        } catch (Exception e) {
            log.error("Error creating account for customer {}: {}", 
                    customerId, e.getMessage(), e);
            throw new RuntimeException("Account creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    // Account number generation handled in Account entity's @PrePersist (12-14 digits)
}

