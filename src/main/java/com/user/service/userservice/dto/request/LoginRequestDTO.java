package com.user.service.userservice.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for user login request
 * Supports login with either email or username along with password
 * Accepts both "email" and "username" field names in JSON
 */
@Data
@Builder
public class LoginRequestDTO {

    /**
     * User identifier - can be either email or username
     * Accepts both "email" and "username" field names in JSON
     */
    @NotBlank(message = "Email or username is required")
    @Size(min = 3, max = 100, message = "Email or username must be between 3 and 100 characters")
    @JsonAlias({"email", "username"})
    private String emailOrUsername;

    /**
     * User password
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
