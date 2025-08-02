package com.onboarding.customer_onboarding.kyc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_verification")
public class KycVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "status")
    private String status; // PENDING / VERIFIED / REJECTED

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "verified_by")
    private Long verifiedBy; // Admin user id as number (foreign key or reference)

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "admin_username", length = 255)
    private String adminUsername; // Stores the admin's username from JWT, not a foreign key

    // Getters and setters
    public Long getVerificationId() { return verificationId; }
    public void setVerificationId(Long verificationId) { this.verificationId = verificationId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Long getVerifiedBy() {
        return verifiedBy;
    }
    public void setVerifiedBy(Long verifiedBy) {
        this.verifiedBy = verifiedBy;
    }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getAdminUsername() {
        return adminUsername;
    }
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
}
