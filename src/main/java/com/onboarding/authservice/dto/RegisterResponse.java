package com.onboarding.authservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private String message;
    private String secret;       // the 2FA secret
    private String qrCodeUrl;

}
