package com.chhavi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chhavi.pojo.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByVoterId(String voterId);

    Optional<User> findByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByVoterId(String voterId);

    boolean existsByMobile(String mobile);
}