package com.onboarding.authservice.repository;

import com.onboarding.authservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByCustomerCustomerId(Long customerId);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByCustomerCustomerId(Long customerId);
}

