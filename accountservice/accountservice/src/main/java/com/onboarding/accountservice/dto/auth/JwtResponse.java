package com.onboarding.accountservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("role")
    private String role;
}
