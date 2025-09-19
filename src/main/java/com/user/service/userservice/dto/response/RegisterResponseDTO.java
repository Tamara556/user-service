package com.user.service.userservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for user registration response
 * Contains user information after successful registration
 */
@Data
@Builder
public class RegisterResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private LocalDateTime createdAt;
    
    @Builder.Default
    private String message = "User registered successfully";

    /**
     * Static factory method for creating registration response
     */
    public static RegisterResponseDTO of(Long id, String username, String email, 
                                       String fullName, LocalDateTime createdAt) {
        return RegisterResponseDTO.builder()
                .id(id)
                .username(username)
                .email(email)
                .fullName(fullName)
                .createdAt(createdAt)
                .build();
    }
}