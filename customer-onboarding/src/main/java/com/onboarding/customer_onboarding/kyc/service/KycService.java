package com.onboarding.customer_onboarding.kyc.service;

import com.onboarding.customer_onboarding.model.Customer;
import com.onboarding.customer_onboarding.kyc.model.KycDocument;
import com.onboarding.customer_onboarding.kyc.model.KycVerification;
import com.onboarding.customer_onboarding.repository.CustomerRepository;
import com.onboarding.customer_onboarding.kyc.repository.KycDocumentRepository;
import com.onboarding.customer_onboarding.kyc.repository.KycVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class KycService {
    @Autowired
    private KycDocumentRepository kycRepo;
    @Autowired
    private CustomerRepository customerRepo;
    @Autowired
    private KycVerificationRepository kycVerificationRepo;

    public void uploadDocument(Long customerId, MultipartFile file, String docType) throws IOException {
        KycDocument doc = new KycDocument();
        doc.setCustomerId(customerId);
        doc.setDocumentType(file.getContentType()); // Set documentType as content type (e.g., application/pdf)
        doc.setFileName(file.getOriginalFilename());
        doc.setContentType(docType.toUpperCase()); // Set contentType as docType (e.g., AADHAAR)
        doc.setData(file.getBytes());
        doc.setUploadedAt(LocalDateTime.now());
        doc.setStatus("PENDING"); // Set status to PENDING by default
        kycRepo.save(doc);
    }

    public void deleteDocumentByFileName(Long customerId, String fileName) {
        List<KycDocument> docs = kycRepo.findByCustomerIdAndFileName(customerId, fileName);
        if (docs.isEmpty()) {
            throw new RuntimeException("Document not found for customerId: " + customerId + " and fileName: " + fileName);
        }
        // Option 1: Delete just one (first) file
        kycRepo.delete(docs.get(0));
        // Option 2: Delete all duplicates
        // kycRepo.deleteAll(docs);
    }

    public List<KycDocument> getDocuments(Long customerId) {
        return kycRepo.findByCustomerId(customerId);
    }

    public List<KycDocument> getDocumentsByCustomerId(Long customerId) {
        return kycRepo.findByCustomerId(customerId);
    }

    public byte[] downloadDocument(Long documentId) {
        return kycRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId))
                .getData();
    }

    public void verifyKyc(Long customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        customer.setKycStatus("VERIFIED");
        customerRepo.save(customer);
    }

    public String updateKycStatus(Long customerId, String status) {
        if (!status.equals("ACCEPTED") && !status.equals("REJECTED") && !status.equals("PENDING")) {
            throw new IllegalArgumentException("Invalid status. Allowed: ACCEPTED, REJECTED, PENDING");
        }
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setKycStatus(status);
        customerRepo.save(customer);
        return "KYC status updated to " + status + " for customer ID: " + customerId;
    }

    @Transactional
    public void verifyKycDocument(Long customerId, Long documentId, String status, String remarks, String adminUsername, Long adminId) {
        if (!"VERIFIED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Status must be VERIFIED or REJECTED");
        }
        KycVerification verification = kycVerificationRepo.findByCustomerIdAndDocumentId(customerId, documentId)
            .orElse(new KycVerification());
        verification.setCustomerId(customerId);
        verification.setDocumentId(documentId);
        verification.setStatus(status.toUpperCase());
        verification.setRemarks(remarks);
        verification.setAdminUsername(adminUsername); // from JWT
        verification.setVerifiedBy(adminId); // as Long, from context or mapping
        verification.setVerifiedAt(LocalDateTime.now());
        kycVerificationRepo.save(verification);
    }
}