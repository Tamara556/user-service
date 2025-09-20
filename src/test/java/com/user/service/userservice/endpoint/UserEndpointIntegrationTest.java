package com.user.service.userservice.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.service.userservice.dto.request.LoginRequestDTO;
import com.user.service.userservice.dto.request.RegisterRequestDTO;
import com.user.service.userservice.entity.User;
import com.user.service.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for UserEndpoint
 * Tests complete request-response cycle including database operations
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Endpoint Integration Tests")
class UserEndpointIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register user successfully with valid data")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.fullName", is("Test User")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.message", is("User registered successfully")));

        assertThat(userRepository.findByUsername("testuser")).isNotNull();
        assertThat(userRepository.findByEmail("test@example.com")).isNotNull();
    }

    @Test
    @DisplayName("Should return bad request when registering user with invalid data")
    void shouldReturnBadRequestWhenRegisteringUserWithInvalidData() throws Exception {
        RegisterRequestDTO invalidRequest = RegisterRequestDTO.builder()
                .username("ab")
                .email("invalid-email")
                .password("123")
                .fullName("Test User")
                .build();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return conflict when registering user with existing username")
    void shouldReturnConflictWhenRegisteringUserWithExistingUsername() throws Exception {
        User existingUser = User.builder()
                .username("testuser")
                .email("existing@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Existing User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(existingUser);

        RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
                .username("testuser")
                .email("new@example.com")
                .password("password123")
                .fullName("New User")
                .build();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Username 'testuser' already exists")));
    }

    @Test
    @DisplayName("Should return conflict when registering user with existing email")
    void shouldReturnConflictWhenRegisteringUserWithExistingEmail() throws Exception {
        User existingUser = User.builder()
                .username("existinguser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Existing User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(existingUser);

        RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
                .username("newuser")
                .email("test@example.com")
                .password("password123")
                .fullName("New User")
                .build();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Email 'test@example.com' already exists")));
    }

    @Test
    @DisplayName("Should login user successfully with valid credentials")
    void shouldLoginUserSuccessfully() throws Exception {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Test User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .emailOrUsername("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", notNullValue()))
                .andExpect(jsonPath("$.userId", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.fullName", is("Test User")))
                .andExpect(jsonPath("$.loginTime", notNullValue()));
    }

    @Test
    @DisplayName("Should login user successfully with username instead of email")
    void shouldLoginUserSuccessfullyWithUsername() throws Exception {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Test User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .emailOrUsername("testuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    @DisplayName("Should return unauthorized when login with invalid credentials")
    void shouldReturnUnauthorizedWhenLoginWithInvalidCredentials() throws Exception {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Test User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .emailOrUsername("test@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid credentials provided")));
    }

    @Test
    @DisplayName("Should return not found when login with non-existent user")
    void shouldReturnNotFoundWhenLoginWithNonExistentUser() throws Exception {
        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .emailOrUsername("nonexistent@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User not found with the provided credentials")));
    }

    @Test
    @DisplayName("Should return bad request when login with invalid data")
    void shouldReturnBadRequestWhenLoginWithInvalidData() throws Exception {
        LoginRequestDTO invalidRequest = LoginRequestDTO.builder()
                .emailOrUsername("ab")
                .password("123")
                .build();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when request body is missing required fields")
    void shouldReturnBadRequestWhenRequestBodyIsMissingRequiredFields() throws Exception {
        String invalidJson = "{\"username\":\"testuser\"}";

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when request body is malformed JSON")
    void shouldReturnBadRequestWhenRequestBodyIsMalformedJson() throws Exception {
        String malformedJson = "{\"username\":\"testuser\",\"password\":\"password123\"";

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return method not allowed for unsupported HTTP methods")
    void shouldReturnMethodNotAllowedForUnsupportedHttpMethods() throws Exception {
        mockMvc.perform(get("/api/user/register"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Should return not found for non-existent endpoints")
    void shouldReturnNotFoundForNonExistentEndpoints() throws Exception {
        mockMvc.perform(post("/api/user/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
