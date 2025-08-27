package com.itsec.technical_test.controller;

import com.itsec.technical_test.entity.AuditLog;
import com.itsec.technical_test.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Audit Logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "List all audit logs")
    public ResponseEntity<List<AuditLog>> all(@AuthenticationPrincipal Object user) {
        // We ignore the authenticated user here, just ensure endpoint is secured
        return ResponseEntity.ok(auditLogService.findAll());
    }
}
