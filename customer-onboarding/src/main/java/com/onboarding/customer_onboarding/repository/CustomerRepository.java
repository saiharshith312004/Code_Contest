package com.onboarding.customer_onboarding.repository;

import com.onboarding.customer_onboarding.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
