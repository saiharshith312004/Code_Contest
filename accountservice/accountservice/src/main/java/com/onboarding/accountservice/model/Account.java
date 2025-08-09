package com.onboarding.accountservice.model;
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
    private Long accountId;
    @Column(unique = true, nullable = false, length = 14)
    private String accountNumber;
    @Column(unique = true, nullable = false)
    private Long customerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
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






