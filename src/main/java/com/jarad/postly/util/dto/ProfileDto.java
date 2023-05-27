package com.jarad.postly.util.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileDto {

    @NotBlank(message = "Username may not be blank")
    @Size(min = 3, max = 255, message = "Username must be between 3 and 36 characters long")
    private String username;

}
