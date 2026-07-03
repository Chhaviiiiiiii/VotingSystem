package com.chhavi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chhavi.pojo.PasswordResetOtp;
import com.chhavi.pojo.User;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findByUserAndUsedFalse(User user);
    List<PasswordResetOtp> findByUser(User user);
}
