package com.user.service.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for user login response
 * Contains JWT token and user information after successful authentication
 */
@Data
@Builder
public class LoginResponseDTO {

    /**
     * JWT token for authenticated user
     */
    private String token;

    /**
     * Token type (Bearer)
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token expiration time in milliseconds
     */
    private Long expiresIn;

    /**
     * User ID
     */
    private Long userId;

    /**
     * Username
     */
    private String username;

    /**
     * User email
     */
    private String email;

    /**
     * User full name
     */
    private String fullName;

    /**
     * Login timestamp
     */
    @Builder.Default
    private LocalDateTime loginTime = LocalDateTime.now();

    /**
     * Static factory method for creating login response
     */
    public static LoginResponseDTO of(String token, Long expiresIn, Long userId, 
                                    String username, String email, String fullName) {
        return LoginResponseDTO.builder()
                .token(token)
                .expiresIn(expiresIn)
                .userId(userId)
                .username(username)
                .email(email)
                .fullName(fullName)
                .build();
    }
}
