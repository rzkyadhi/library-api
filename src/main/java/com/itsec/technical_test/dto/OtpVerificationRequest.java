package com.itsec.technical_test.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerificationRequest {
    private String usernameOrEmail;
    private String otp;
}
