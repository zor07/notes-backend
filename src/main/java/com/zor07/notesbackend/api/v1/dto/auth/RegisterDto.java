package com.zor07.notesbackend.api.v1.dto.auth;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public record RegisterDto(
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9._-]*$", message = "Username must start with a letter and contain only latin letters, digits, dot, underscore or dash")
        @Size(min = 3, max = 50, message = "Username length must be between 3 and 50 characters")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password length must be between 6 and 100 characters")
        String password,
        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 100, message = "Name length must be between 1 and 100 characters")
        String name
) {
}

