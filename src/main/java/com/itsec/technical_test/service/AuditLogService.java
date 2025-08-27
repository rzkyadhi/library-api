package com.itsec.technical_test.service;

import com.itsec.technical_test.entity.AuditLog;
import com.itsec.technical_test.entity.User;
import com.itsec.technical_test.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public void log(User user, String action, String detail, String userAgent) {
        AuditLog log = new AuditLog()
                .setUser(user)
                .setAction(action)
                .setDetail(detail)
                .setUserAgent(userAgent)
                .setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}
