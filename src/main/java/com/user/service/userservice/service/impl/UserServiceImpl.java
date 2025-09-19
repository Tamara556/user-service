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
import com.user.service.userservice.service.UserService;
import com.user.service.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public RegisterResponseDTO registerUser(RegisterRequestDTO registerRequestDTO) {
        log.info("Attempting to register user with username: {}", registerRequestDTO.getUsername());
        try {
            if (userRepository.findByUsername(registerRequestDTO.getUsername()) != null) {
                log.warn("Registration failed: Username '{}' already exists", registerRequestDTO.getUsername());
                throw new UsernameAlreadyExistsException(registerRequestDTO.getUsername());
            }
            if (userRepository.findByEmail(registerRequestDTO.getEmail()) != null) {
                log.warn("Registration failed: Email '{}' already exists", registerRequestDTO.getEmail());
                throw new EmailAlreadyExistsException(registerRequestDTO.getEmail());
            }
            User user = userMapper.toEntity(registerRequestDTO);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            log.info("User registered successfully with ID: {}", savedUser.getId());
            return RegisterResponseDTO.of(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getCreatedAt()
            );
        } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user registration: {}", e.getMessage(), e);
            throw new UserRegistrationException("Failed to register user", e);
        }
    }

    @Override
    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        log.info("Attempting to login user with identifier: {}", loginRequestDTO.getEmailOrUsername());
        try {
            User user = userRepository.findByEmailOrUsername(loginRequestDTO.getEmailOrUsername());
            if (user == null) {
                log.warn("Login failed: User not found with identifier: {}", loginRequestDTO.getEmailOrUsername());
                throw new UserNotFoundException("User not found with the provided credentials");
            }

            if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
                log.warn("Login failed: Invalid password for user: {}", user.getUsername());
                throw new InvalidCredentialsException("Invalid credentials provided");
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail());
            log.info("User logged in successfully: {}", user.getUsername());

            return LoginResponseDTO.of(
                token,
                jwtUtil.getExpirationTime(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName()
            );

        } catch (UserNotFoundException | InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user login: {}", e.getMessage(), e);
            throw new InvalidCredentialsException("Login failed due to an unexpected error", e);
        }
    }
}
