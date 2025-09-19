package com.user.service.userservice.endpoint;

import com.user.service.userservice.dto.request.LoginRequestDTO;
import com.user.service.userservice.dto.request.RegisterRequestDTO;
import com.user.service.userservice.dto.response.LoginResponseDTO;
import com.user.service.userservice.dto.response.RegisterResponseDTO;
import com.user.service.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user-related operations
 * Handles user registration and authentication endpoints
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserEndpoint {

    private final UserService userService;

    /**
     * Register a new user
     *
     * @param registerRequestDTO registration request containing user details
     * @return ResponseEntity with registration response
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        log.info("Received registration request for username: {}", registerRequestDTO.getUsername());
        RegisterResponseDTO response = userService.registerUser(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate user and generate JWT token
     *
     * @param loginRequestDTO login request containing credentials
     * @return ResponseEntity with login response including JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        log.info("Received login request for identifier: {}", loginRequestDTO.getEmailOrUsername());
        LoginResponseDTO response = userService.loginUser(loginRequestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user profile (protected endpoint)
     *
     * @return ResponseEntity with user profile information
     */
    @GetMapping("/profile")
    public ResponseEntity<String> getUserProfile() {
        log.info("Accessing user profile endpoint");
        return ResponseEntity.ok("User profile accessed successfully! This is a protected endpoint.");
    }
}
