package com.onboarding.accountservice.service;

import com.onboarding.accountservice.model.Account;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    void createAccountForCustomer(Long customerId);
    
    Optional<Account> findByCustomerId(Long customerId);
    
    List<Account> findAllAccounts();
    
    Optional<Account> findByAccountNumber(String accountNumber);
}