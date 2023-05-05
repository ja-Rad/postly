package com.jarad.postly.util.dto;

import com.jarad.postly.entity.Role;
import com.jarad.postly.validation.PasswordMatches;
import com.jarad.postly.validation.ValidEmail;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@PasswordMatches
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotNull
    @NotEmpty
    @ValidEmail
    private String email;

    @NotNull
    @NotEmpty
    private String password;

    private String matchingPassword;

    private Set<Role> roles = new HashSet<>();
}
