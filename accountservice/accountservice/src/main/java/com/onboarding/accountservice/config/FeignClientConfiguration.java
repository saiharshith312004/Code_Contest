package com.onboarding.accountservice.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.onboarding.accountservice.client")
public class FeignClientConfiguration {
    // Configuration class to enable Feign clients
}
