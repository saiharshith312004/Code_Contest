package com.onboarding.customer_onboarding.config;

import com.onboarding.customer_onboarding.kyc.security.AdminJwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Autowired
    private AdminJwtFilter adminJwtFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeHttpRequests()
            .requestMatchers("/api/kyc/verify/**").permitAll()  // Let AdminJwtFilter handle auth
            .anyRequest().permitAll();
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<AdminJwtFilter> adminJwtFilterRegistration() {
        FilterRegistrationBean<AdminJwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(adminJwtFilter);  // Use injected filter
        registrationBean.addUrlPatterns("/api/kyc/verify/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
