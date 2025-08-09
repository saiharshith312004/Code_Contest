package com.onboarding.accountservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.codec.ErrorDecoder.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Configuration
@Slf4j
public class AuthServiceClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() >= 400 && response.status() <= 499) {
                log.error("Client error occurred while calling {}: status={}, body={}",
                        methodKey, response.status(), response.body());
                return new ResponseStatusException(
                        HttpStatus.valueOf(response.status()),
                        String.format("Client error occurred while calling %s", methodKey)
                );
            } else if (response.status() >= 500) {
                log.error("Server error occurred while calling {}: status={}", methodKey, response.status());
                return new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        String.format("Server error occurred while calling %s", methodKey)
                );
            }
            return new Default().decode(methodKey, response);
        };
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(1000, 2000, 3);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Requested-With", "XMLHttpRequest");
            requestTemplate.header("Accept", "application/json");
        };
    }
}
