package com.itsec.technical_test.controller;

import com.itsec.technical_test.dto.AuthResponse;
import com.itsec.technical_test.dto.LoginRequest;
import com.itsec.technical_test.dto.RegisterRequest;
import com.itsec.technical_test.dto.RefreshTokenRequest;
import com.itsec.technical_test.dto.OtpVerificationRequest;
import com.itsec.technical_test.dto.MessageResponse;
import com.itsec.technical_test.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.register(request,
                servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/login")
    @Operation(summary = "Initiate login and send OTP")
    public ResponseEntity<MessageResponse> login(@RequestBody LoginRequest request,
                                                 HttpServletRequest servletRequest) {
        authService.login(request, servletRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(new MessageResponse("OTP sent to your email"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and authenticate user")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody OtpVerificationRequest request,
                                                  HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.verifyOtp(request,
                servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request,
                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.refresh(request,
                servletRequest.getHeader("User-Agent")));
    }
}
