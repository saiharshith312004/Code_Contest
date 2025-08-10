package com.onboarding.customer_onboarding.kyc.controller;
import com.onboarding.customer_onboarding.kyc.dto.KycRequestDTO;
import com.onboarding.customer_onboarding.kyc.service.KycService;
import com.onboarding.customer_onboarding.kyc.model.KycDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/kyc")
public class KycController {

    @Autowired
    private KycService kycService;

    // Add this method explicitly for /requests path
    @GetMapping("/requests")
    public ResponseEntity<List<KycRequestDTO>> getPendingKycRequests() {
        List<KycRequestDTO> pendingRequests = kycService.getPendingKycRequests();
        if (pendingRequests.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pendingRequests);
    }


    @PostMapping("/upload/{customerId}")
    public ResponseEntity<String> uploadKycDocument(
            @PathVariable Long customerId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("type") String docType) {
        try {
            kycService.uploadDocument(customerId, file, docType);
            return ResponseEntity.status(HttpStatus.CREATED).body("KYC document uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload document.");
        }
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<KycDocument>> getDocumentsByCustomerId(@PathVariable Long customerId) {
        List<KycDocument> documents = kycService.getDocumentsByCustomerId(customerId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/download/{docId}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long docId) {
        KycDocument document = kycService.getDocumentById(docId); // Add this method if needed
        byte[] data = document.getData();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(document.getContentType())); // use actual MIME type
        headers.setContentDispositionFormData("inline", document.getFileName()); // to display inline in browser

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }


    @PostMapping("/verify/{customerId}")
    public ResponseEntity<String> verifyKyc(@PathVariable Long customerId) {
        kycService.verifyKyc(customerId);
        return ResponseEntity.ok("KYC verified successfully.");
    }

    @DeleteMapping("/delete/{customerId}/{fileName:.+}")
    public ResponseEntity<String> deleteKycFile(
            @PathVariable Long customerId,
            @PathVariable String fileName) {
        kycService.deleteDocumentByFileName(customerId, fileName);
        return ResponseEntity.ok("Document deleted successfully.");
    }

    @PutMapping("/verify/{customerId}")
    public String updateKycStatus(@PathVariable Long customerId, @RequestParam String status) {
        kycService.updateKycStatus(customerId, status.toUpperCase());
        return "KYC status updated to " + status.toUpperCase();
    }

    @GetMapping("/{customerId}/status")
    public ResponseEntity<String> getKycStatus(@PathVariable Long customerId) {
        String status = kycService.getKycStatus(customerId); // implement in service
        return ResponseEntity.ok(status != null ? status : "NOT_SUBMITTED");
    }
}






