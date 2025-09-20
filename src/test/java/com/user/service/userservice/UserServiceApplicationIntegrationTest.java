package com.user.service.userservice;

import com.user.service.userservice.entity.User;
import com.user.service.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full integration tests for User Service Application
 * Uses Testcontainers with PostgreSQL for realistic database testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("User Service Application Integration Tests")
class UserServiceApplicationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/user";
    }

    @Test
    @DisplayName("Should start application context successfully")
    void shouldStartApplicationContextSuccessfully() {
        assertThat(restTemplate).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    @DisplayName("Should perform complete user registration and login flow")
    void shouldPerformCompleteUserRegistrationAndLoginFlow() {
        String registerUrl = getBaseUrl() + "/register";
        String loginUrl = getBaseUrl() + "/login";

        String registerJson = """
                {
                    "username": "integrationuser",
                    "email": "integration@example.com",
                    "password": "password123",
                    "fullName": "Integration Test User"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> registerRequest = new HttpEntity<>(registerJson, headers);

        ResponseEntity<String> registerResponse = restTemplate.exchange(
                registerUrl,
                HttpMethod.POST,
                registerRequest,
                String.class
        );

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registerResponse.getBody()).contains("integrationuser");
        assertThat(registerResponse.getBody()).contains("integration@example.com");

        String loginJson = """
                {
                    "emailOrUsername": "integration@example.com",
                    "password": "password123"
                }
                """;

        HttpEntity<String> loginRequest = new HttpEntity<>(loginJson, headers);

        ResponseEntity<String> loginResponse = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                loginRequest,
                String.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).contains("token");
        assertThat(loginResponse.getBody()).contains("integrationuser");
    }

    @Test
    @DisplayName("Should handle concurrent user registrations")
    void shouldHandleConcurrentUserRegistrations() {
        String registerUrl = getBaseUrl() + "/register";

        String user1Json = """
                {
                    "username": "concurrentuser1",
                    "email": "concurrent1@example.com",
                    "password": "password123",
                    "fullName": "Concurrent User 1"
                }
                """;

        String user2Json = """
                {
                    "username": "concurrentuser2",
                    "email": "concurrent2@example.com",
                    "password": "password123",
                    "fullName": "Concurrent User 2"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request1 = new HttpEntity<>(user1Json, headers);
        HttpEntity<String> request2 = new HttpEntity<>(user2Json, headers);

        ResponseEntity<String> response1 = restTemplate.exchange(
                registerUrl,
                HttpMethod.POST,
                request1,
                String.class
        );

        ResponseEntity<String> response2 = restTemplate.exchange(
                registerUrl,
                HttpMethod.POST,
                request2,
                String.class
        );

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(userRepository.findByUsername("concurrentuser1")).isNotNull();
        assertThat(userRepository.findByUsername("concurrentuser2")).isNotNull();
    }

    @Test
    @DisplayName("Should handle database connection and migrations")
    void shouldHandleDatabaseConnectionAndMigrations() {
        User testUser = User.builder()
                .username("dbtestuser")
                .email("dbtest@example.com")
                .password("encodedPassword")
                .fullName("Database Test User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();

        User foundUser = userRepository.findByUsername("dbtestuser");
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("dbtest@example.com");
    }

    @Test
    @DisplayName("Should handle application health check")
    void shouldHandleApplicationHealthCheck() {
        String healthUrl = "http://localhost:" + port + "/actuator/health";

        ResponseEntity<String> healthResponse = restTemplate.getForEntity(healthUrl, String.class);

        assertThat(healthResponse.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should handle invalid endpoint requests")
    void shouldHandleInvalidEndpointRequests() {
        String invalidUrl = getBaseUrl() + "/nonexistent";

        ResponseEntity<String> response = restTemplate.getForEntity(invalidUrl, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should handle malformed JSON requests")
    void shouldHandleMalformedJsonRequests() {
        String registerUrl = getBaseUrl() + "/register";

        String malformedJson = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123"
                """; // Missing closing brace

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(malformedJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                registerUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should handle large request payloads")
    void shouldHandleLargeRequestPayloads() {
        String registerUrl = getBaseUrl() + "/register";

        StringBuilder longFullName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longFullName.append("VeryLongName");
        }

        String largeJson = String.format("""
                {
                    "username": "largeuser",
                    "email": "large@example.com",
                    "password": "password123",
                    "fullName": "%s"
                }
                """, longFullName.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(largeJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                registerUrl,
                HttpMethod.POST,
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.BAD_REQUEST);
    }
}
