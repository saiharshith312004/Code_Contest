package com.onboarding.authservice.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDashboardResponse {
    private String username;

    private String fullName;
    private LocalDate dob;
    private String email;
    private String phone;
    private String address;
    private String pan;
    private String aadhaar;
    private String kycStatus;
    private Long customerId;
    private String accountNumber;

}
