package com.onboarding.authservice.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Customer Onboarding AuthService API")
                .version("1.0")
                .description("authservice")
                .contact(new Contact()
                    .name("Sai Harshith")
                    .email("support@onboarding.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://springdoc.org"))
            );
    }
}