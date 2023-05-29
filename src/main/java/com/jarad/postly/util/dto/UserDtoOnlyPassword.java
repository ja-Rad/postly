package com.jarad.postly.util.dto;

import com.jarad.postly.util.annotation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@PasswordMatches
public class UserDtoOnlyPassword {

    @NotBlank(message = "Password may not be blank")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
    private String password;

    private String matchingPassword;
}
