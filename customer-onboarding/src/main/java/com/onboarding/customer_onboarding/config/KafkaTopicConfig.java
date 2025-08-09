package com.onboarding.customer_onboarding.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    
    @Value("${kafka.topic.kyc-verified}")
    private String kycVerifiedTopic;
    
    @Value("${kafka.topic.customer-onboarded}")
    private String customerOnboardedTopic;
    
    @Bean
    public NewTopic kycVerifiedTopic() {
        return new NewTopic(kycVerifiedTopic, 1, (short) 1);
    }
    
    @Bean
    public NewTopic customerOnboardedTopic() {
        return new NewTopic(customerOnboardedTopic, 1, (short) 1);
    }
}