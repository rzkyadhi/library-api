package com.itsec.technical_test.dto;

import com.itsec.technical_test.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Full Name is Required")
    private String fullName;

    @NotBlank(message = "Username is Required")
    private String username;

    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid Email Format")
    private String email;

    @NotBlank(message = "Password is Required")
    @Size(min = 8, message = "Password must be at least 6 characters long")
    private String password;
    private Role role;
}
