package com.onboarding.customer_onboarding.kyc.service;

import com.onboarding.customer_onboarding.kyc.dto.KycDocumentsDTO;
import com.onboarding.customer_onboarding.kyc.dto.KycRequestDTO;
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

@Service
@Slf4j
public class KycService {

    private final KycDocumentRepository kycDocumentRepo;
    private final CustomerRepository customerRepo;
    private final KycVerificationRepository kycVerificationRepo;
    private final KycEventProducer kycEventProducer;

    @Autowired
    public KycService(KycDocumentRepository kycDocumentRepo,
                      CustomerRepository customerRepo,
                      KycVerificationRepository kycVerificationRepo,
                      KycEventProducer kycEventProducer) {
        this.kycDocumentRepo = kycDocumentRepo;
        this.customerRepo = customerRepo;
        this.kycVerificationRepo = kycVerificationRepo;
        this.kycEventProducer = kycEventProducer;
    }

    public void uploadDocument(Long customerId, MultipartFile file, String docType) throws IOException {
        KycDocument doc = new KycDocument();
        doc.setCustomerId(customerId);
        doc.setDocumentType(docType.toUpperCase());  // Fix here: store document type from param
        doc.setFileName(file.getOriginalFilename());
        doc.setContentType(file.getContentType());   // contentType is MIME type here
        doc.setData(file.getBytes());
        doc.setUploadedAt(LocalDateTime.now());
        doc.setStatus("PENDING");
        kycDocumentRepo.save(doc);

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        customer.setKycStatus("PENDING");
        customerRepo.save(customer);
    }

    public List<KycDocument> getDocumentsByCustomerId(Long customerId) {
        return kycDocumentRepo.findByCustomerId(customerId);
    }

    public void deleteDocumentByFileName(Long customerId, String fileName) {
        List<KycDocument> docs = kycDocumentRepo.findByCustomerIdAndFileName(customerId, fileName);
        if (docs.isEmpty()) {
            throw new RuntimeException("Document not found for customerId: " + customerId + " and fileName: " + fileName);
        }
        kycDocumentRepo.delete(docs.get(0));
    }

    public List<KycDocument> getDocuments(Long customerId) {
        return kycDocumentRepo.findByCustomerId(customerId);
    }

    public byte[] downloadDocument(Long documentId) {
        return kycDocumentRepo.findById(documentId)
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
        log.info("Starting KYC status update for customer: {}, new status: {}", customerId, status);

        if (!status.equals("ACCEPTED") && !status.equals("REJECTED") && !status.equals("PENDING")) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        String oldStatus = customer.getKycStatus();
        customer.setKycStatus(status);
        customerRepo.save(customer);

        log.info("Updated KYC status for customer {} from {} to {}", customerId, oldStatus, status);

        if ("ACCEPTED".equals(status)) {
            kycEventProducer.publishKycVerifiedEvent(customerId);
        } else if ("REJECTED".equals(status)) {
            kycEventProducer.publishKycRejectedEvent(customerId);
        }

        return "KYC status updated to " + status + " for customer ID: " + customerId;
    }


    @Transactional
    public void updateCustomerKycStatus(Long customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        List<KycDocument> documents = kycDocumentRepo.findByCustomerId(customerId);

        if (documents.isEmpty()) {
            customer.setKycStatus("PENDING");
            customerRepo.save(customer);
            return;
        }

        boolean hasRejected = documents.stream()
                .anyMatch(doc -> "REJECTED".equalsIgnoreCase(doc.getStatus()));
        if (hasRejected) {
            customer.setKycStatus("REJECTED");
            customerRepo.save(customer);
            return;
        }

        boolean allVerified = documents.stream()
                .allMatch(doc -> "VERIFIED".equalsIgnoreCase(doc.getStatus()));
        customer.setKycStatus(allVerified ? "ACCEPTED" : "PENDING");
        customerRepo.save(customer);
    }

    @Transactional
    public void verifyKycDocument(Long customerId, Long documentId, String newStatus, String remarks,
                                  String adminUsername, Long adminId) {
        String normalizedStatus = newStatus != null ? newStatus.toUpperCase() : "";
        if (!"VERIFIED".equals(normalizedStatus) && !"REJECTED".equals(normalizedStatus) && !"PENDING".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Status must be VERIFIED, REJECTED, or PENDING");
        }

        if (!customerRepo.existsById(customerId)) {
            throw new EntityNotFoundException("Customer not found with id: " + customerId);
        }

        KycDocument document = kycDocumentRepo.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        if (!document.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Document " + documentId + " does not belong to customer " + customerId);
        }

        document.setStatus(normalizedStatus);
        kycDocumentRepo.save(document);

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

        updateCustomerKycStatus(customerId);
    }

    public String getKycStatus(Long customerId) {
        return customerRepo.findById(customerId)
                .map(Customer::getKycStatus)
                .orElse(null);
    }

    public List<KycDocument> getAllKycRequests() {
        return kycDocumentRepo.findAll();
    }

    public List<KycRequestDTO> getPendingKycRequests() {
        List<Customer> customers = customerRepo.findByKycStatusIgnoreCase("PENDING");

        return customers.stream().map(customer -> {
            KycRequestDTO dto = new KycRequestDTO();
            dto.setCustomerId(customer.getCustomerId());
            dto.setFullName(customer.getFullName());
            dto.setAadhaar(customer.getAadhaar());
            dto.setPan(customer.getPan());
            dto.setDob(customer.getDob());
            dto.setAddress(customer.getAddress());
            dto.setKycStatus(customer.getKycStatus());

            List<KycDocumentsDTO> docs = kycDocumentRepo.findByCustomerId(customer.getCustomerId())
                    .stream()
                    .map(doc -> new KycDocumentsDTO(
                            doc.getId(),
                            doc.getDocumentType(),  // use documentType for doc type (AADHAAR/PAN)
                            doc.getFileName()
                    ))
                    .toList();


            dto.setDocuments(docs);

            return dto;
        }).toList();
    }

    // In KycService.java

    public KycDocument getDocumentById(Long documentId) {
        return kycDocumentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
    }


}
