package com.onboarding.authservice.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "AUDIT_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "action", nullable = false)
    private String action;
    @Column(name = "entity_type",nullable = false)
    private String entityType;
    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}