package com.jarad.postly.util.dto;

import com.jarad.postly.util.annotation.ValidEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDtoOnlyEmail {

    @NotBlank(message = "Email may not be blank")
    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters long")
    @ValidEmail
    private String email;

}
