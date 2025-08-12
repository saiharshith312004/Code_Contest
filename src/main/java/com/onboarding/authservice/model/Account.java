package com.onboarding.authservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Random;
@Entity
@Table(name = "ACCOUNTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", updatable = false, nullable = false)
    private Long accountId;

    @Column(name = "account_number", unique = true, nullable = false, length = 14)
    private String accountNumber;


    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.accountNumber == null) {
            this.accountNumber = generateAccountNumber();
        }
    }

    private String generateAccountNumber() {
        Random random = new Random();
        int length = 12 + random.nextInt(3); // 12 to 14 digits
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}




