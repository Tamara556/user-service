package com.user.service.userservice.util;


import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SignatureException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Unit tests for JwtUtil
 * Tests JWT token generation, validation, and claim extraction
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Utility Tests")
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String SECRET_KEY = "mySecretKeyForTestingPurposesOnlyThisShouldBeLongEnough";
    private static final Long EXPIRATION_TIME = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_TIME);
    }

    @Test
    @DisplayName("Should generate valid JWT token with user information")
    void shouldGenerateValidJwtToken() {
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);

        String extractedUsername = jwtUtil.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void shouldExtractUsernameFromValidToken() {
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);
        String extractedUsername = jwtUtil.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Should extract expiration date from valid token")
    void shouldExtractExpirationDateFromValidToken() {
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);
        Date expiration = jwtUtil.extractExpiration(token);

        assertThat(expiration).isNotNull();
        assertThat(expiration.getTime()).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    @DisplayName("Should validate token successfully with correct username")
    void shouldValidateTokenSuccessfully() {
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);
        Boolean isValid = jwtUtil.validateToken(token, username);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false when validating token with incorrect username")
    void shouldReturnFalseWhenValidatingTokenWithIncorrectUsername() {
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);
        Boolean isValid = jwtUtil.validateToken(token, "wronguser");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when validating malformed token")
    void shouldReturnFalseWhenValidatingMalformedToken() {
        String malformedToken = "malformed.token.here";
        Boolean isValid = jwtUtil.validateToken(malformedToken, "testuser");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when validating token with wrong signature")
    void shouldReturnFalseWhenValidatingTokenWithWrongSignature() {
        String tokenWithWrongSignature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQyNjIyfQ.wrongsignature";
        Boolean isValid = jwtUtil.validateToken(tokenWithWrongSignature, "testuser");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false when validating expired token")
    void shouldReturnFalseWhenValidatingExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1ms expiration

        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);

        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Boolean isValid = jwtUtil.validateToken(token, username);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when extracting username from malformed token")
    void shouldThrowExceptionWhenExtractingUsernameFromMalformedToken() {
        String malformedToken = "malformed.token.here";

        assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
                .isInstanceOfAny(MalformedJwtException.class, SignatureException.class);
    }

    @Test
    @DisplayName("Should throw exception when extracting expiration from malformed token")
    void shouldThrowExceptionWhenExtractingExpirationFromMalformedToken() {
        String malformedToken = "malformed.token.here";

        assertThatThrownBy(() -> jwtUtil.extractExpiration(malformedToken))
                .isInstanceOfAny(MalformedJwtException.class, SignatureException.class);
    }

    @Test
    @DisplayName("Should return correct expiration time")
    void shouldReturnCorrectExpirationTime() {
        Long expirationTime = jwtUtil.getExpirationTime();

        assertThat(expirationTime).isEqualTo(EXPIRATION_TIME);
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1ms expiration

        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);

        try {
            TimeUnit.MILLISECONDS.sleep(10); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Boolean isExpired = jwtUtil.isTokenExpired(token);
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("Should detect non-expired token")
    void shouldDetectNonExpiredToken() {
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);
        Boolean isExpired = jwtUtil.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should extract custom claims from token")
    void shouldExtractCustomClaimsFromToken() {
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";

        String token = jwtUtil.generateToken(userId, username, email);

        Long extractedUserId = jwtUtil.extractClaim(token, claims -> claims.get("userId", Long.class));
        String extractedEmail = jwtUtil.extractClaim(token, claims -> claims.get("email", String.class));

        assertThat(extractedUserId).isEqualTo(userId);
        assertThat(extractedEmail).isEqualTo(email);
    }
}
