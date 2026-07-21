package com.chhavi.dao;

import com.chhavi.pojo.User;
import com.chhavi.pojo.PendingRegistration;

public interface AuthDao {

    PendingRegistration registerUser(User user);

    boolean verifyEmailOtp(String email, String otp);

    void requestPasswordReset(String email);

    boolean verifyOtp(String email, String otp);

    boolean resetPassword(String email, String otp, String newPassword);
}