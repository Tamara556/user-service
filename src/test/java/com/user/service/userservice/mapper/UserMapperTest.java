package com.user.service.userservice.mapper;


import com.user.service.userservice.dto.request.RegisterRequestDTO;
import com.user.service.userservice.dto.response.RegisterResponseDTO;
import com.user.service.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserMapper
 * Tests mapping between DTOs and entities
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Mapper Tests")
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    private RegisterRequestDTO registerRequestDTO;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequestDTO = RegisterRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should map RegisterRequestDTO to User entity successfully")
    void shouldMapRegisterRequestDtoToUserEntity() {
        User result = userMapper.toEntity(registerRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(registerRequestDTO.getUsername());
        assertThat(result.getEmail()).isEqualTo(registerRequestDTO.getEmail());
        assertThat(result.getPassword()).isEqualTo(registerRequestDTO.getPassword());
        assertThat(result.getFullName()).isEqualTo(registerRequestDTO.getFullName());
        assertThat(result.getId()).isNull();
        assertThat(result.getCreatedAt()).isNull();
        assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should return null when mapping null RegisterRequestDTO")
    void shouldReturnNullWhenMappingNullRegisterRequestDto() {
        User result = userMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should map User entity to RegisterResponseDTO successfully")
    void shouldMapUserEntityToRegisterResponseDto() {
        RegisterResponseDTO result = userMapper.toRegisterResponseDTO(user);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getFullName()).isEqualTo(user.getFullName());
        assertThat(result.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(result.getMessage()).isEqualTo("User registered successfully");
    }

    @Test
    @DisplayName("Should return null when mapping null User entity")
    void shouldReturnNullWhenMappingNullUserEntity() {
        RegisterResponseDTO result = userMapper.toRegisterResponseDTO(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should map RegisterRequestDTO with null fullName to User entity")
    void shouldMapRegisterRequestDtoWithNullFullNameToUserEntity() {
        RegisterRequestDTO requestWithNullFullName = RegisterRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName(null)
                .build();

        User result = userMapper.toEntity(requestWithNullFullName);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.getFullName()).isNull();
    }

    @Test
    @DisplayName("Should map User entity with null fullName to RegisterResponseDTO")
    void shouldMapUserEntityWithNullFullNameToRegisterResponseDto() {
        User userWithNullFullName = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        RegisterResponseDTO result = userMapper.toRegisterResponseDTO(userWithNullFullName);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFullName()).isNull();
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should map RegisterRequestDTO with empty fullName to User entity")
    void shouldMapRegisterRequestDtoWithEmptyFullNameToUserEntity() {
        RegisterRequestDTO requestWithEmptyFullName = RegisterRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("")
                .build();

        User result = userMapper.toEntity(requestWithEmptyFullName);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.getFullName()).isEqualTo("");
    }
}
