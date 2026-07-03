package com.chhavi.daoimpl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.chhavi.dao.UserDao;
import com.chhavi.pojo.User;
import com.chhavi.repository.UserRepository;

@Repository
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    public UserDaoImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean mobileExists(String mobile) {
        return userRepository.existsByMobile(mobile);
    }

    @Override
    public boolean voterIdExists(String voterId) {
        return userRepository.existsByVoterId(voterId);
    }
}