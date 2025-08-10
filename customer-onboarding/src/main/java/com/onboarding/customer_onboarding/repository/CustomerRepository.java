package com.onboarding.customer_onboarding.repository;

import com.onboarding.customer_onboarding.kyc.model.KycDocument;
import com.onboarding.customer_onboarding.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByKycStatusIgnoreCase(String kycStatus);

}
