package com.onboarding.customer_onboarding.kyc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class KycEventProducer {
    private static final Logger log = LoggerFactory.getLogger(KycEventProducer.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String kycVerifiedTopic;

    public KycEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                          @Value("${kafka.topic.kyc-verified}") String kycVerifiedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.kycVerifiedTopic = kycVerifiedTopic;
        log.info("Initialized KycEventProducer with topic: {}", kycVerifiedTopic);
    }

    public void publishKycVerifiedEvent(Long customerId) {
        log.info("Entering publishKycVerifiedEvent for customer: {}", customerId);
        
        if (customerId == null) {
            log.warn("Attempted to publish KYC verified event with null customer ID");
            return;
        }

        String eventMessage = String.format("KYC_VERIFIED:%d", customerId);
        log.info("Preparing to publish message to topic {}: {}", kycVerifiedTopic, eventMessage);
        
        try {
            log.debug("Sending message to Kafka topic: {}", kycVerifiedTopic);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(kycVerifiedTopic, eventMessage);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published KYC_VERIFIED event for Customer ID: {}, offset: {}, partition: {}, timestamp: {}", 
                            customerId, 
                            result.getRecordMetadata().offset(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().timestamp());
                } else {
                    log.error("Failed to publish KYC_VERIFIED event for Customer ID: {}", customerId, ex);
                }
            });
            
            // Add a timeout to prevent hanging
            future.orTimeout(10, TimeUnit.SECONDS);
            
        } catch (KafkaException e) {
            log.error("Kafka error while publishing KYC_VERIFIED event for Customer ID: {}", customerId, e);
            throw e; // Re-throw to be handled by the caller
        } catch (Exception e) {
            log.error("Unexpected error while publishing KYC_VERIFIED event for Customer ID: {}", customerId, e);
            throw new RuntimeException("Failed to publish KYC verified event", e);
        } finally {
            log.info("Exiting publishKycVerifiedEvent for customer: {}", customerId);
        }
    }
}