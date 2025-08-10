package com.onboarding.customer_onboarding.kyc.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class KycDocumentsDTO {
    private Long documentId;
    private String documentType;
    private String fileName;

    public KycDocumentsDTO(Long documentId, String documentType, String fileName) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.fileName = fileName;
    }




}