package com.onboarding.accountservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountKafkaListener {

    private static final String TOPIC = "${kafka.topic.kyc-verified}";
    private final AccountService accountService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicInteger retryCount = new AtomicInteger(0);

    private Long extractCustomerId(String message) {
        // Handle KYC_VERIFIED:123 format
        if (message.startsWith("KYC_VERIFIED:")) {
            try {
                return Long.parseLong(message.split(":")[1].trim());
            } catch (Exception e) {
                log.error("Invalid KYC_VERIFIED format. Expected KYC_VERIFIED:<number>. Got: {}", message);
                return null;
            }
        }
        
        // Try to parse as a simple number
        try {
            return Long.parseLong(message.trim());
        } catch (NumberFormatException e) {
            // Not a number, continue to JSON parsing
        }
        
        // Try to parse as JSON
        try {
            KycVerifiedEvent event = objectMapper.readValue(message, KycVerifiedEvent.class);
            return event.getCustomerId();
        } catch (JsonProcessingException jsonEx) {
            log.error("Invalid message format. Expected KYC_VERIFIED:<number> or JSON. Got: {}", message);
            return null;
        }
    }
    
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        exclude = { IllegalArgumentException.class }
    )
    @KafkaListener(
        topics = TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeKycVerifiedEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Received message on topic {}: {}", TOPIC, record);
        long startTime = System.currentTimeMillis();
        String message = record.value();
        
        try {
            log.info("Received KYC_VERIFIED event: {}", message);
            
            if (message == null || message.trim().isEmpty()) {
                log.warn("Received empty message, skipping processing");
                ack.acknowledge();
                return;
            }
            
            // Extract customer ID using the helper method
            Long customerId = extractCustomerId(message);
            
            if (customerId == null) {
                log.error("No valid customer ID found in message: {}", message);
                ack.acknowledge();
                return;
            }
            
            log.info("Processing KYC_VERIFIED event for customerId: {} (attempt {}/{})", 
                    customerId, retryCount.incrementAndGet(), 3);
            
            accountService.createAccountForCustomer(customerId);
            
            long endTime = System.currentTimeMillis();
            log.info("Successfully processed account creation for customerId: {} in {} ms", 
                    customerId, (endTime - startTime));
            
        } catch (Exception e) {
            log.error("Failed to process KYC_VERIFIED event. Message: {}. Error: {}", 
                    message, e.getMessage(), e);
            throw new RuntimeException("Failed to process KYC_VERIFIED event", e);
        } finally {
            ack.acknowledge();
            retryCount.set(0);
        }
    }
    
    private KycVerifiedEvent parseEvent(String message) throws JsonProcessingException {
        try {
            return objectMapper.readValue(message, KycVerifiedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse KYC event: {}", message);
            throw e;
        }
    }
}