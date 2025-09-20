package com.user.service.userservice.repository;

import com.user.service.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for UserRepository
 * Tests database operations and custom query methods
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getFullName()).isEqualTo("Test User");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        entityManager.persistAndFlush(testUser);

        User foundUser = userRepository.findByUsername("testuser");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return null when finding user by non-existent username")
    void shouldReturnNullWhenFindingUserByNonExistentUsername() {
        User foundUser = userRepository.findByUsername("nonexistent");

        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        entityManager.persistAndFlush(testUser);

        User foundUser = userRepository.findByEmail("test@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return null when finding user by non-existent email")
    void shouldReturnNullWhenFindingUserByNonExistentEmail() {
        User foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("Should find user by email using findByEmailOrUsername")
    void shouldFindUserByEmailUsingFindByEmailOrUsername() {
        entityManager.persistAndFlush(testUser);

        User foundUser = userRepository.findByEmailOrUsername("test@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should find user by username using findByEmailOrUsername")
    void shouldFindUserByUsernameUsingFindByEmailOrUsername() {
        entityManager.persistAndFlush(testUser);

        User foundUser = userRepository.findByEmailOrUsername("testuser");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return null when finding user by non-existent identifier using findByEmailOrUsername")
    void shouldReturnNullWhenFindingUserByNonExistentIdentifierUsingFindByEmailOrUsername() {
        User foundUser = userRepository.findByEmailOrUsername("nonexistent");

        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        User savedUser = entityManager.persistAndFlush(testUser);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty optional when finding user by non-existent ID")
    void shouldReturnEmptyOptionalWhenFindingUserByNonExistentId() {
        Optional<User> foundUser = userRepository.findById(999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password1")
                .fullName("User One")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password2")
                .fullName("User Two")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        var allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getUsername).containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        User savedUser = entityManager.persistAndFlush(testUser);

        userRepository.delete(savedUser);
        entityManager.flush();

        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        User savedUser = entityManager.persistAndFlush(testUser);

        savedUser.setFullName("Updated Full Name");
        savedUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getFullName()).isEqualTo("Updated Full Name");
        assertThat(updatedUser.getUpdatedAt()).isNotNull();
        assertThat(updatedUser.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
    }

    @Test
    @DisplayName("Should enforce unique constraint on username")
    void shouldEnforceUniqueConstraintOnUsername() {
        User user1 = User.builder()
                .username("duplicate")
                .email("user1@example.com")
                .password("password1")
                .fullName("User One")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .username("duplicate") // Same username
                .email("user2@example.com")
                .password("password2")
                .fullName("User Two")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(user1);

        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(user2);
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should enforce unique constraint on email")
    void shouldEnforceUniqueConstraintOnEmail() {
        User user1 = User.builder()
                .username("user1")
                .email("duplicate@example.com")
                .password("password1")
                .fullName("User One")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("duplicate@example.com") // Same email
                .password("password2")
                .fullName("User Two")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(user1);

        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(user2);
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle null values in optional fields")
    void shouldHandleNullValuesInOptionalFields() {
        User userWithNullFullName = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName(null) // Null full name
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(userWithNullFullName);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getFullName()).isNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }
}
