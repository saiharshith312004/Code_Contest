package com.onboarding.authservice.repository;
import com.onboarding.authservice.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
