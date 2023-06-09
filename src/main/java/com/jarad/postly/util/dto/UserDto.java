package com.jarad.postly.util.dto;

import com.jarad.postly.entity.Role;
import com.jarad.postly.util.validation.PasswordMatches;
import com.jarad.postly.util.validation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@PasswordMatches
public class UserDto {

    @NotBlank(message = "Email may not be blank")
    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters long")
    @ValidEmail
    private String email;

    @NotBlank(message = "Password may not be blank")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
    private String password;

    private String matchingPassword;

    private String verificationCode;

    private boolean enabled;

    private Set<Role> roles = new LinkedHashSet<>();
}
