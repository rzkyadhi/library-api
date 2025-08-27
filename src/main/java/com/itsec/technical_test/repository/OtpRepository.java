package com.itsec.technical_test.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.itsec.technical_test.entity.Otp;
import com.itsec.technical_test.entity.User;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByUser(User user);
    Optional<Otp> findByUserAndCode(User user, String code);
    void deleteByUser(User user);
}
