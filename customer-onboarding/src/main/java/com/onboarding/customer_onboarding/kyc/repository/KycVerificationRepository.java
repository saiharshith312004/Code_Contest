package com.onboarding.customer_onboarding.kyc.repository;

import com.onboarding.customer_onboarding.kyc.model.KycVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface KycVerificationRepository extends JpaRepository<KycVerification, Long> {
    Optional<KycVerification> findByCustomerIdAndDocumentId(Long customerId, Long documentId);
    List<KycVerification> findByCustomerId(Long customerId);
}
