package com.itsec.technical_test.dto;

import com.itsec.technical_test.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String fullName;
    private String username;
    private String email;
    private String password;
    private Role role;
}
