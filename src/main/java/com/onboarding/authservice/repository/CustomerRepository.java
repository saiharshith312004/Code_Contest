package com.onboarding.authservice.repository;
import com.onboarding.authservice.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByAadhaar(String aadhaar);
    Optional<Customer> findByPan(String pan);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUser_UserId(Long user_id);
}