package com.user.service.userservice.exception;


import com.user.service.userservice.config.TestSecurityConfig;
import com.user.service.userservice.dto.request.RegisterRequestDTO;
import com.user.service.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for GlobalExceptionHandler
 * Tests exception handling and error response formatting
 */
@WebMvcTest
@Import(TestSecurityConfig.class)
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("Should handle UsernameAlreadyExistsException")
    void shouldHandleUsernameAlreadyExistsException() throws Exception {
        when(userService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new UsernameAlreadyExistsException("testuser"));

        String requestJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123",
                    "fullName": "Test User"
                }
                """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Username 'testuser' already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle EmailAlreadyExistsException")
    void shouldHandleEmailAlreadyExistsException() throws Exception {
        when(userService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("test@example.com"));

        String requestJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123",
                    "fullName": "Test User"
                }
                """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Email 'test@example.com' already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle UserNotFoundException")
    void shouldHandleUserNotFoundException() throws Exception {
        when(userService.loginUser(any()))
                .thenThrow(new UserNotFoundException("User not found"));

        String requestJson = """
                {
                    "emailOrUsername": "nonexistent@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle InvalidCredentialsException")
    void shouldHandleInvalidCredentialsException() throws Exception {
        when(userService.loginUser(any()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        String requestJson = """
                {
                    "emailOrUsername": "test@example.com",
                    "password": "wrongpassword"
                }
                """;

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle UserRegistrationException")
    void shouldHandleUserRegistrationException() throws Exception {
        when(userService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new UserRegistrationException("Registration failed", new RuntimeException("Database connection failed")));

        String requestJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123",
                    "fullName": "Test User"
                }
                """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Registration failed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle validation errors")
    void shouldHandleValidationErrors() throws Exception {
        String invalidRequestJson = """
                {
                    "username": "ab",
                    "email": "invalid-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        String malformedJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123"
                """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle generic runtime exceptions")
    void shouldHandleGenericRuntimeExceptions() throws Exception {
        when(userService.registerUser(any(RegisterRequestDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String requestJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123",
                    "fullName": "Test User"
                }
                """;

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
