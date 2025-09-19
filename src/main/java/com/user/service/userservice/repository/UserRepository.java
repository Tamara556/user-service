package com.user.service.userservice.repository;

import com.user.service.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    
    /**
     * Find user by either email or username
     *
     * @param emailOrUsername email or username to search for
     * @return User entity if found, null otherwise
     */
    @Query("SELECT u FROM User u WHERE u.email = :emailOrUsername OR u.username = :emailOrUsername")
    User findByEmailOrUsername(@Param("emailOrUsername") String emailOrUsername);
}
