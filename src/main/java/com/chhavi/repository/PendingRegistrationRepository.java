package com.chhavi.repository;

import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.chhavi.pojo.PendingRegistration;

@Repository
public interface PendingRegistrationRepository extends MongoRepository<PendingRegistration, String> {
    Optional<PendingRegistration> findByEmail(String email);
    void deleteByEmail(String email);
    void deleteByOtpExpiryBefore(LocalDateTime dateTime);
}
