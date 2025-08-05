package com.onboarding.customer_onboarding.kyc.service;

import com.onboarding.customer_onboarding.kyc.model.KycDocument;
import com.onboarding.customer_onboarding.kyc.model.KycVerification;
import com.onboarding.customer_onboarding.kyc.repository.KycDocumentRepository;
import com.onboarding.customer_onboarding.kyc.repository.KycVerificationRepository;
import com.onboarding.customer_onboarding.model.Customer;
import com.onboarding.customer_onboarding.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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

    /**
     * Updates the customer's KYC status based on their document statuses.
     * Rules:
     * - If any document is REJECTED, overall status is REJECTED
     * - If all required documents are VERIFIED, status is ACCEPTED
     * - If any required document is missing or not verified, status is PENDING
     * 
     * @param customerId The ID of the customer to update
     * @throws EntityNotFoundException if customer is not found
     */
    @Transactional
    public void updateCustomerKycStatus(Long customerId) {
        // Get customer or throw exception if not found
        Customer customer = customerRepo.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // Get all documents for this customer
        List<KycDocument> documents = kycRepo.findByCustomerId(customerId);
        
        // If no documents, set status to PENDING
        if (documents.isEmpty()) {
            customer.setKycStatus("PENDING");
            customerRepo.save(customer);
            return;
        }
        
        // Check for any REJECTED documents
        boolean hasRejected = documents.stream()
            .anyMatch(doc -> doc.getStatus() != null && "REJECTED".equalsIgnoreCase(doc.getStatus()));
        
        if (hasRejected) {
            customer.setKycStatus("REJECTED");
            customerRepo.save(customer);
            return;
        }

        // Check if all documents are VERIFIED
        boolean allVerified = documents.stream()
            .allMatch(doc -> doc.getStatus() != null && "VERIFIED".equalsIgnoreCase(doc.getStatus()));
        
        // Update customer status
        customer.setKycStatus(allVerified ? "ACCEPTED" : "PENDING");
        customerRepo.save(customer);
        
        // Log the status update
        log.info("Updated KYC status for customer {} to {}", customerId, customer.getKycStatus());
    }

    /**
     * Verifies or rejects a KYC document and updates the customer's KYC status accordingly.
     * Status transition rules:
     * - VERIFIED → VERIFIED: Restricted (no change allowed)
     * - VERIFIED → REJECTED: Allowed
     * - VERIFIED → PENDING: Allowed
     * - REJECTED → PENDING: Allowed
     * - REJECTED → VERIFIED: Allowed
     * 
     * @param customerId The ID of the customer
     * @param documentId The ID of the document to verify/reject
     * @param newStatus The new status (VERIFIED/REJECTED/PENDING)
     * @param remarks Optional remarks from the admin
     * @param adminUsername Username of the admin performing the action
     * @param adminId ID of the admin performing the action
     * @throws IllegalArgumentException if status transition is not allowed or document doesn't belong to customer
     * @throws EntityNotFoundException if document or customer is not found
     * @throws IllegalStateException if trying to change status from VERIFIED to VERIFIED
     */
    @Transactional
    public void verifyKycDocument(Long customerId, Long documentId, String newStatus, String remarks, 
                                String adminUsername, Long adminId) {
        // Validate input status
        String normalizedStatus = newStatus != null ? newStatus.toUpperCase() : "";
        if (!"VERIFIED".equals(normalizedStatus) && 
            !"REJECTED".equals(normalizedStatus) && 
            !"PENDING".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Status must be VERIFIED, REJECTED, or PENDING");
        }
        
        // Verify customer exists
        if (!customerRepo.existsById(customerId)) {
            throw new EntityNotFoundException("Customer not found with id: " + customerId);
        }
        
        // Get the document
        KycDocument document = kycRepo.findById(documentId)
            .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));
            
        // Verify the document belongs to the specified customer
        if (!document.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException(
                String.format("Document %d does not belong to customer %d", documentId, customerId));
        }
        
        // Get current status (null-safe)
        String currentStatus = document.getStatus() != null ? document.getStatus().toUpperCase() : "PENDING";
        
        // Apply status transition rules
        if ("VERIFIED".equals(currentStatus) && "VERIFIED".equals(normalizedStatus)) {
            throw new IllegalStateException("Cannot change status from VERIFIED to VERIFIED");
        }
        
        // All other transitions are allowed as per requirements
        log.info("Changing document {} status from {} to {} for customer {}", 
                documentId, currentStatus, normalizedStatus, customerId);
        
        // Update document status
        document.setStatus(normalizedStatus);
        kycRepo.save(document);
        
        // Create or update the verification record
        KycVerification verification = kycVerificationRepo.findByCustomerIdAndDocumentId(customerId, documentId)
            .orElseGet(() -> {
                KycVerification newVerification = new KycVerification();
                newVerification.setCustomerId(customerId);
                newVerification.setDocumentId(documentId);
                return newVerification;
            });
            
        verification.setStatus(normalizedStatus);
        verification.setRemarks(remarks);
        verification.setAdminUsername(adminUsername);
        verification.setVerifiedBy(adminId);
        verification.setVerifiedAt(LocalDateTime.now());
        kycVerificationRepo.save(verification);
        
        log.info("Updated verification record for document {} (customer: {})", documentId, customerId);
        
        // Update customer's overall KYC status based on all documents
        updateCustomerKycStatus(customerId);
    }
}