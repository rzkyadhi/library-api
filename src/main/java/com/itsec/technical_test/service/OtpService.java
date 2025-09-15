package com.itsec.technical_test.service;

import com.itsec.technical_test.entity.Otp;
import com.itsec.technical_test.entity.User;
import com.itsec.technical_test.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    public boolean generateAndSendOtp(User user) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        Otp otp = otpRepository.findByUser(user).orElse(new Otp());
        otp.setUser(user)
                .setCode(code)
                .setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is " + code);
        try {
            mailSender.send(message);
            return true;
        } catch (MailException e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            return false;
        }
    }

    public boolean validateOtp(User user, String code) {
        return otpRepository.findByUserAndCode(user, code)
                .filter(o -> o.getExpiryTime().isAfter(LocalDateTime.now()))
                .map(o -> { otpRepository.delete(o); return true; })
                .orElse(false);
    }
}
