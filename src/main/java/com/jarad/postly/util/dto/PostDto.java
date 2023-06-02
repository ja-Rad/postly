package com.jarad.postly.util.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostDto {

    @NotBlank(message = "Title may not be blank")
    @Size(min = 6, max = 60, message = "Title must be between 6 and 60 characters long")
    private String title;

    @NotBlank(message = "Description may not be blank")
    @Size(min = 250, max = 5000, message = "Description must be between 250 and 5000 characters long")
    private String description;

}
