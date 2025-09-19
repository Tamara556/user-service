# User Service API Documentation

## Overview
The User Service provides authentication and user management functionality for the Online Learning Platform. It supports user registration and login with JWT token-based authentication.

## Base URL
```
http://localhost:8081/api/user
```

## Authentication
The service uses JWT (JSON Web Token) for authentication. After successful login, include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. User Registration

**POST** `/register`

Register a new user in the system.

#### Request Body
```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "fullName": "John Doe"
}
```

#### Validation Rules
- `username`: Required, 3-50 characters, must be unique
- `email`: Required, valid email format, must be unique
- `password`: Required, minimum 8 characters
- `fullName`: Optional

#### Success Response (201 Created)
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### Error Responses
- **400 Bad Request**: Validation errors
- **409 Conflict**: Username or email already exists
- **500 Internal Server Error**: Registration failed

### 2. User Login

**POST** `/login`

Authenticate user and receive JWT token.

#### Request Body
```json
{
  "emailOrUsername": "john.doe@example.com",
  "password": "securePassword123"
}
```

#### Validation Rules
- `emailOrUsername`: Required, 3-100 characters (can be email or username)
- `password`: Required, minimum 8 characters

#### Success Response (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "loginTime": "2024-01-15T10:30:00"
}
```

#### Error Responses
- **400 Bad Request**: Validation errors
- **401 Unauthorized**: Invalid credentials
- **404 Not Found**: User not found
- **500 Internal Server Error**: Login failed

## Error Response Format

### Standard Error Response
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Validation Error Response
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "fieldErrors": {
    "username": "Username is required",
    "email": "Email should be valid"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

## Security Features

### Password Security
- Passwords are hashed using BCrypt before storage
- Minimum password length: 8 characters
- Passwords are never returned in API responses

### JWT Token Security
- Tokens expire after 24 hours
- Tokens contain user ID, username, and email
- Tokens are signed with HMAC SHA-256

### Input Validation
- All inputs are validated using Bean Validation
- SQL injection protection through JPA
- XSS protection through proper encoding

## Usage Examples

### cURL Examples

#### Register User
```bash
curl -X POST http://localhost:8081/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "fullName": "John Doe"
  }'
```

#### Login User
```bash
curl -X POST http://localhost:8081/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "john.doe@example.com",
    "password": "securePassword123"
  }'
```

#### Using JWT Token
```bash
curl -X GET http://localhost:8081/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Java Code Examples

#### Using Builder Pattern for DTOs
```java
// Creating LoginRequestDTO using builder
LoginRequestDTO loginRequest = LoginRequestDTO.builder()
    .emailOrUsername("john.doe@example.com")
    .password("securePassword123")
    .build();

// Creating RegisterRequestDTO using builder
RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
    .username("johndoe")
    .email("john.doe@example.com")
    .password("securePassword123")
    .fullName("John Doe")
    .build();

// Using static factory methods for responses
LoginResponseDTO loginResponse = LoginResponseDTO.of(
    token, expiresIn, userId, username, email, fullName
);

RegisterResponseDTO registerResponse = RegisterResponseDTO.of(
    id, username, email, fullName, createdAt
);
```

## Configuration

### JWT Configuration
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000 # 24 hours
```

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_service_db
    username: postgres
    password: root
```

## Logging

The service provides comprehensive logging:
- **INFO**: Successful operations (registration, login)
- **WARN**: Business logic issues (duplicate users, invalid credentials)
- **ERROR**: System errors and exceptions
- **DEBUG**: Detailed operation flow

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Development Guidelines

### Code Quality
- Follows SOLID principles
- Implements DRY (Don't Repeat Yourself)
- Follows KISS (Keep It Simple, Stupid)
- Implements YAGNI (You Aren't Gonna Need It)

### Error Handling
- Comprehensive exception handling
- Meaningful error messages
- Proper HTTP status codes
- Detailed logging for debugging

### Testing
- Unit tests for service layer
- Integration tests for endpoints
- Validation tests for DTOs
- Security tests for authentication
