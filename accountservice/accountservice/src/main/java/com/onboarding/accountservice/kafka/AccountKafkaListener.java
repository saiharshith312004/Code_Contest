package com.onboarding.accountservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onboarding.accountservice.service.AccountService;
import com.onboarding.accountservice.service.EmailService;  // add your email service here
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

    private static final String VERIFIED_TOPIC = "${kafka.topic.kyc-verified}";
    private static final String REJECTED_TOPIC = "${kafka.topic.kyc-rejected}";

    private final AccountService accountService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicInteger retryCount = new AtomicInteger(0);

    private Long extractCustomerId(String message) {
        if (message.startsWith("KYC_VERIFIED:") || message.startsWith("KYC_REJECTED:")) {
            try {
                return Long.parseLong(message.split(":")[1].trim());
            } catch (Exception e) {
                log.error("Invalid KYC event format. Expected KYC_VERIFIED:<number> or KYC_REJECTED:<number>. Got: {}", message);
                return null;
            }
        }

        try {
            return Long.parseLong(message.trim());
        } catch (NumberFormatException e) {
            // Not a number, continue to JSON parsing
        }

        try {
            KycEvent event = objectMapper.readValue(message, KycEvent.class);
            return event.getCustomerId();
        } catch (JsonProcessingException jsonEx) {
            log.error("Invalid message format. Expected KYC event format or JSON. Got: {}", message);
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
            topics = VERIFIED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeKycVerifiedEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Received message on topic {}: {}", VERIFIED_TOPIC, record);
        long startTime = System.currentTimeMillis();
        String message = record.value();

        try {
            if (message == null || message.trim().isEmpty()) {
                log.warn("Received empty message, skipping processing");
                ack.acknowledge();
                return;
            }

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

    // New consumer for KYC_REJECTED events
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            exclude = { IllegalArgumentException.class }
    )
    @KafkaListener(
            topics = REJECTED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeKycRejectedEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Received message on topic {}: {}", REJECTED_TOPIC, record);
        String message = record.value();

        try {
            if (message == null || message.trim().isEmpty()) {
                log.warn("Received empty rejected message, skipping processing");
                ack.acknowledge();
                return;
            }

            Long customerId = extractCustomerId(message);
            if (customerId == null) {
                log.error("No valid customer ID found in rejected message: {}", message);
                ack.acknowledge();
                return;
            }

            log.info("Processing KYC_REJECTED event for customerId: {}", customerId);

            emailService.sendKycRejectedEmail(customerId);

            log.info("Sent KYC rejection email to customerId: {}", customerId);

        } catch (Exception e) {
            log.error("Failed to process KYC_REJECTED event. Message: {}. Error: {}",
                    message, e.getMessage(), e);
            throw new RuntimeException("Failed to process KYC_REJECTED event", e);
        } finally {
            ack.acknowledge();
        }
    }

    // You can define a simple generic KycEvent class to parse JSON if needed
    public static class KycEvent {
        private Long customerId;
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
    }
}
