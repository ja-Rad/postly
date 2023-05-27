package com.jarad.postly.util.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDto {

    @NotBlank(message = "Description may not be blank")
    @Size(min = 3, max = 2000, message = "Description must be between 3 and 2000 characters long")
    private String description;
}
