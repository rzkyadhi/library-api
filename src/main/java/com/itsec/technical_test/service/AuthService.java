package com.itsec.technical_test.service;

import com.itsec.technical_test.dto.AuthResponse;
import com.itsec.technical_test.dto.LoginRequest;
import com.itsec.technical_test.dto.RegisterRequest;
import com.itsec.technical_test.dto.RefreshTokenRequest;
import com.itsec.technical_test.dto.OtpVerificationRequest;
import com.itsec.technical_test.entity.Role;
import com.itsec.technical_test.entity.User;
import com.itsec.technical_test.repository.UserRepository;
import com.itsec.technical_test.security.JwtService;
import com.itsec.technical_test.security.TokenService;
import com.itsec.technical_test.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final OtpService otpService;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse register(RegisterRequest request, String userAgent) {
        if (userRepository.existsByUsername(request.getUsername()) ||
            userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Role role = request.getRole() == null ? Role.VIEWER : request.getRole();

        User user = new User();
        user.setFullName(request.getFullName())
                .setUsername(request.getUsername())
                .setEmail(request.getEmail())
                .setPassword(passwordEncoder.encode(request.getPassword()))
                .setRole(role);
        // Default role for newly registered users is VIEWER
        userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        tokenService.storeAccessToken(accessToken, jwtService.getAccessExpiration());
        tokenService.storeRefreshToken(user.getUsername(), refreshToken, jwtService.getRefreshExpiration());
        auditLogService.log(user, "REGISTER", null, userAgent);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void login(LoginRequest request, String userAgent) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        LocalDateTime now = LocalDateTime.now();
        if (user.getAccountLockedUntil() != null) {
            if (user.getAccountLockedUntil().isAfter(now)) {
                throw new ResponseStatusException(HttpStatus.LOCKED);
            } else {
                user.setAccountLockedUntil(null).setFailedLoginAttempts(0).setLastFailedLoginTime(null);
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword()));
            User authenticated = (User) authentication.getPrincipal();
            authenticated.setFailedLoginAttempts(0).setLastFailedLoginTime(null).setAccountLockedUntil(null);
            userRepository.save(authenticated);
            if (!otpService.generateAndSendOtp(authenticated)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email");
            }
            auditLogService.log(authenticated, "LOGIN_SUCCESS", null, userAgent);
        } catch (BadCredentialsException e) {
            if (user.getLastFailedLoginTime() == null || user.getLastFailedLoginTime().isBefore(now.minusMinutes(10))) {
                user.setFailedLoginAttempts(1);
            } else {
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            }
            user.setLastFailedLoginTime(now);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLockedUntil(now.plusMinutes(30));
                user.setFailedLoginAttempts(0);
            }
            userRepository.save(user);
            auditLogService.log(user, "LOGIN_FAILURE", null, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerificationRequest request, String userAgent) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        if (!otpService.validateOtp(user, request.getOtp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        String accessToken = jwtService.generateAccessToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        tokenService.storeAccessToken(accessToken, jwtService.getAccessExpiration());
        tokenService.storeRefreshToken(user.getUsername(), refreshToken, jwtService.getRefreshExpiration());
        auditLogService.log(user, "OTP_VERIFIED", null, userAgent);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request, String userAgent) {
        String oldRefreshToken = request.getRefreshToken();
        String username = tokenService.getUsernameFromRefreshToken(oldRefreshToken);
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        if (!jwtService.isTokenValid(oldRefreshToken, user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        tokenService.deleteRefreshToken(username);
        String accessToken = jwtService.generateAccessToken(username);
        String refreshToken = jwtService.generateRefreshToken(username);
        tokenService.storeAccessToken(accessToken, jwtService.getAccessExpiration());
        tokenService.storeRefreshToken(username, refreshToken, jwtService.getRefreshExpiration());
        auditLogService.log(user, "REFRESH_TOKEN", null, userAgent);
        return new AuthResponse(accessToken, refreshToken);
    }
}

