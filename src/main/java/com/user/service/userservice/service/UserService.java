package com.user.service.userservice.service;

import com.user.service.userservice.dto.request.LoginRequestDTO;
import com.user.service.userservice.dto.request.RegisterRequestDTO;
import com.user.service.userservice.dto.response.LoginResponseDTO;
import com.user.service.userservice.dto.response.RegisterResponseDTO;

public interface UserService {
    /**
     * Register a new user with the provided registration details
     *
     * @param registerRequestDTO the registration request containing username, email, password, and fullName
     * @return RegisterResponseDTO containing the created user details
     * @throws IllegalArgumentException if username or email already exists
     */
    RegisterResponseDTO registerUser(RegisterRequestDTO registerRequestDTO);

    /**
     * Authenticate user and generate JWT token
     *
     * @param loginRequestDTO the login request containing email/username and password
     * @return LoginResponseDTO containing JWT token and user details
     * @throws UserNotFoundException if user is not found
     * @throws InvalidCredentialsException if credentials are invalid
     */
    LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO);
}
