package com.onboarding.customer_onboarding.kyc.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_documents2")
public class KycDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DOCUMENT_ID")
    private Long id;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "DOCUMENT_TYPE")
    private String documentType;

    @Column(name = "BASE64_DATA")
    private String base64Data;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "CONTENT_TYPE")
    private String contentType;

    @Lob
    @Column(name = "DATA")
    private byte[] data;

    @Column(name = "FILE_NAME")
    private String fileName;

    // Getters and setters for all fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getBase64Data() { return base64Data; }
    public void setBase64Data(String base64Data) { this.base64Data = base64Data; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}