package com.user.service.userservice.service.impl;


import com.user.service.userservice.dto.request.LoginRequestDTO;
import com.user.service.userservice.dto.request.RegisterRequestDTO;
import com.user.service.userservice.dto.response.LoginResponseDTO;
import com.user.service.userservice.dto.response.RegisterResponseDTO;
import com.user.service.userservice.entity.User;
import com.user.service.userservice.exception.EmailAlreadyExistsException;
import com.user.service.userservice.exception.InvalidCredentialsException;
import com.user.service.userservice.exception.UserNotFoundException;
import com.user.service.userservice.exception.UserRegistrationException;
import com.user.service.userservice.exception.UsernameAlreadyExistsException;
import com.user.service.userservice.mapper.UserMapper;
import com.user.service.userservice.repository.UserRepository;
import com.user.service.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserServiceImpl
 * Tests all business logic scenarios including success and failure cases
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Implementation Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private User user;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequestDTO = RegisterRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        loginRequestDTO = LoginRequestDTO.builder()
                .emailOrUsername("test@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        savedUser = User.builder()
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
    @DisplayName("Should register user successfully when all data is valid")
    void shouldRegisterUserSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(userMapper.toEntity(registerRequestDTO)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponseDTO result = userService.registerUser(registerRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFullName()).isEqualTo("Test User");
        assertThat(result.getCreatedAt()).isNotNull();

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toEntity(registerRequestDTO);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UsernameAlreadyExistsException when username already exists")
    void shouldThrowUsernameAlreadyExistsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(savedUser);

        assertThatThrownBy(() -> userService.registerUser(registerRequestDTO))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("Username 'testuser' already exists");

        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).findByEmail(anyString());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email already exists")
    void shouldThrowEmailAlreadyExistsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(savedUser);

        assertThatThrownBy(() -> userService.registerUser(registerRequestDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email 'test@example.com' already exists");

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserRegistrationException when unexpected error occurs")
    void shouldThrowUserRegistrationException() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(userMapper.toEntity(registerRequestDTO)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> userService.registerUser(registerRequestDTO))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Failed to register user");

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toEntity(registerRequestDTO);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void shouldLoginUserSuccessfully() {
        when(userRepository.findByEmailOrUsername("test@example.com")).thenReturn(savedUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser", "test@example.com")).thenReturn("jwtToken");
        when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

        LoginResponseDTO result = userService.loginUser(loginRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwtToken");
        assertThat(result.getExpiresIn()).isEqualTo(86400000L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFullName()).isEqualTo("Test User");
        assertThat(result.getLoginTime()).isNotNull();

        verify(userRepository).findByEmailOrUsername("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil).generateToken(1L, "testuser", "test@example.com");
        verify(jwtUtil).getExpirationTime();
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowUserNotFoundException() {
        when(userRepository.findByEmailOrUsername("test@example.com")).thenReturn(null);

        assertThatThrownBy(() -> userService.loginUser(loginRequestDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with the provided credentials");

        verify(userRepository).findByEmailOrUsername("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
    void shouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmailOrUsername("test@example.com")).thenReturn(savedUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.loginUser(loginRequestDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials provided");

        verify(userRepository).findByEmailOrUsername("test@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil, never()).generateToken(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when unexpected error occurs during login")
    void shouldThrowInvalidCredentialsExceptionOnUnexpectedError() {
        when(userRepository.findByEmailOrUsername("test@example.com")).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> userService.loginUser(loginRequestDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Login failed due to an unexpected error");

        verify(userRepository).findByEmailOrUsername("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should login user successfully with username instead of email")
    void shouldLoginUserSuccessfullyWithUsername() {
        LoginRequestDTO usernameLoginRequest = LoginRequestDTO.builder()
                .emailOrUsername("testuser")
                .password("password123")
                .build();

        when(userRepository.findByEmailOrUsername("testuser")).thenReturn(savedUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser", "test@example.com")).thenReturn("jwtToken");
        when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

        LoginResponseDTO result = userService.loginUser(usernameLoginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwtToken");
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findByEmailOrUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil).generateToken(1L, "testuser", "test@example.com");
    }
}
