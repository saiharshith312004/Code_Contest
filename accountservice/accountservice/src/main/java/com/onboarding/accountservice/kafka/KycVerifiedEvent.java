package com.onboarding.accountservice.kafka;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycVerifiedEvent {
    private Long customerId;
}