package com.onboarding.customer_onboarding.kyc.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class KycRequestDTO {
    private Long customerId;
    private String fullName;
    private String aadhaar; // Aadhaar number
    private String pan;     // PAN number
    private LocalDate dob;
    private String address;
    private String kycStatus;

    // NEW: document IDs for preview/download
    private Long aadhaarDocId;
    private Long panDocId;

    // NEW: full document list (for setDocuments)
    private List<KycDocumentsDTO> documents;
}
