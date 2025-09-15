package com.itsec.technical_test.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Username or Email is Required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is Required")
    private String password;
}
