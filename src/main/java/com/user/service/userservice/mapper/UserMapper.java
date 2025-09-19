package com.user.service.userservice.mapper;

import com.user.service.userservice.dto.request.RegisterRequestDTO;
import com.user.service.userservice.dto.response.RegisterResponseDTO;
import com.user.service.userservice.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequestDTO registerRequestDTO) {
        if (registerRequestDTO == null) {
            return null;
        }

        return User.builder()
                .username(registerRequestDTO.getUsername())
                .email(registerRequestDTO.getEmail())
                .password(registerRequestDTO.getPassword())
                .fullName(registerRequestDTO.getFullName())
                .build();
    }

    
    public RegisterResponseDTO toRegisterResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return RegisterResponseDTO.of(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getCreatedAt()
        );
    }
}
