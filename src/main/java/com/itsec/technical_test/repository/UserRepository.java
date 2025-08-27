package com.itsec.technical_test.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itsec.technical_test.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
