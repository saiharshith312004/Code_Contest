package com.onboarding.customer_onboarding.kyc.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.onboarding.customer_onboarding.kyc.model.KycDocument;

public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {
    List<KycDocument> findByCustomerId(Long customerId);
    
    List<KycDocument> findByCustomerIdAndFileName(Long customerId, String fileName);
    void deleteByCustomerIdAndFileName(Long customerId, String fileName);
}