package com.itsec.technical_test.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerificationRequest {
    @NotBlank(message = "Username or Email is Required")
    private String usernameOrEmail;
    @NotBlank(message = "OTP is Required")
    private String otp;
}
