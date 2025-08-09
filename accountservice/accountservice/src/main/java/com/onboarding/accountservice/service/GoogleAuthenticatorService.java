package com.onboarding.accountservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class GoogleAuthenticatorService {

    /**
     * Generates a TOTP code using the provided base32 secret key.
     * @param base32Secret The base32-encoded secret key for TOTP generation
     * @return A 6-digit TOTP code as a string
     */
    public String generateTotpCode(String base32Secret) {
        if (base32Secret == null || base32Secret.trim().isEmpty()) {
            throw new IllegalArgumentException("TOTP secret cannot be null or empty");
        }
        
        log.info("Generating TOTP code with secret: {}", base32Secret);
        
        try {
            // Get current time window (30 seconds)
            long timeWindow = System.currentTimeMillis() / 30000;
            log.debug("Time window: {}", timeWindow);
            
            // Decode the base32 secret
            Base32 base32 = new Base32();
            byte[] key = base32.decode(base32Secret);
            log.debug("Decoded key (hex): {}", bytesToHex(key));
            
            // Prepare data for hashing
            byte[] data = new byte[8];
            long value = timeWindow;
            
            for (int i = 8; i-- > 0; value >>>= 8) {
                data[i] = (byte) value;
            }
            
            log.debug("Data to hash (hex): {}", bytesToHex(data));
            
            // Initialize HMAC-SHA1
            SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            
            // Generate the hash
            byte[] hash = mac.doFinal(data);
            log.debug("Generated hash (hex): {}", bytesToHex(hash));
            
            // Extract the 4-byte dynamic binary code
            int offset = hash[hash.length - 1] & 0xF;
            log.debug("Offset: {}", offset);
            
            // Get the 4 bytes starting at the offset
            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            
            // Truncate to 31 bits and get the last 6 digits
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= 1000000;
            
            String totpCode = String.format("%06d", truncatedHash);
            log.info("Generated TOTP code: {}", totpCode);
            
            return totpCode;
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating TOTP code: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate TOTP code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to convert byte array to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
