package com.onboarding.authservice.model;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
@Entity
@Table(name = "CUSTOMERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Column(name = "dob", nullable = false)
    private LocalDate dob;
    @Email(message = "Invalid Email format")
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "phone", nullable = false, unique = true)
    private String phone;
    @Column(name = "address", nullable = false)
    private String address;
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "invalid PAN number")
    @Column(name = "pan", nullable = false)
    private String pan;
    @Column(name = "aadhaar", nullable = false, length = 12)
    private String aadhaar;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;
}