package com.chhavi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.chhavi.dao.AuthDao;
import com.chhavi.dto.ForgotPasswordDto;
import com.chhavi.dto.OtpVerificationDto;
import com.chhavi.dto.ResetPasswordDto;
import com.chhavi.pojo.User;
import com.chhavi.utils.EmailService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final AuthDao authDao;
    private final EmailService emailService;

    public AuthController(AuthDao authDao, EmailService emailService) {
        this.authDao = authDao;
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String showHomePage() {
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam("profileImageFile") org.springframework.web.multipart.MultipartFile profileImageFile,
            Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            if (profileImageFile != null && !profileImageFile.isEmpty()) {
                try {
                    byte[] bytes = profileImageFile.getBytes();
                    String base64Image = "data:" + profileImageFile.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
                    user.setProfileImage(base64Image);
                } catch (Exception e) {
                    System.err.println("Failed to encode profile image during registration: " + e.getMessage());
                }
            } else {
                user.setProfileImage("https://api.dicebear.com/7.x/adventurer/svg?seed=" + user.getFullName().replaceAll("\\s+", ""));
            }

            User savedUser = authDao.registerUser(user);
            emailService.sendRegistrationVerificationOtpEmail(savedUser.getEmail(), savedUser.getVerificationOtp());
            return "redirect:/verify-email-otp?email=" + savedUser.getEmail();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify-email-otp")
    public String showVerifyEmailOtpPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify-email-otp";
    }

    @PostMapping("/verify-email-otp")
    public String verifyEmailOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        boolean verified = authDao.verifyEmailOtp(email, otp);
        if (verified) {
            return "redirect:/login?verified=true";
        } else {
            model.addAttribute("email", email);
            model.addAttribute("error", "Invalid or expired OTP.");
            return "verify-email-otp";
        }
    }

    @PostMapping("/resend-verification-otp")
    public String resendVerificationOtp(@RequestParam String email, Model model) {
        try {
            authDao.sendRegistrationOtp(email);
            model.addAttribute("email", email);
            model.addAttribute("success", "A new OTP has been sent to your email.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
        }
        return "verify-email-otp";
    }

    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "verified", required = false) String verified,
            HttpServletRequest request,
            Model model) {

        if (error != null) {
            HttpSession session = request.getSession(false);
            String errorMessage = "Invalid email or password.";
            if (session != null) {
                Object lastException = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
                if (lastException instanceof org.springframework.security.core.AuthenticationException) {
                    String exceptionMessage = ((org.springframework.security.core.AuthenticationException) lastException).getMessage();
                    if ("Bad credentials".equalsIgnoreCase(exceptionMessage)) {
                        errorMessage = "Invalid email or password.";
                    } else {
                        errorMessage = exceptionMessage;
                    }
                }
            }
            model.addAttribute("error", errorMessage);
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }
        if (verified != null) {
            model.addAttribute("success", "Email verified successfully. You can now login.");
        }
        return "login";
    }

    @GetMapping("/login-success")
    public String loginSuccess(org.springframework.security.core.Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/voter/dashboard";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute("forgotPasswordDto") ForgotPasswordDto dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "forgot-password";
        }

        try {
            authDao.requestPasswordReset(dto.getEmail());
            model.addAttribute("success", "If an account exists for this email, an OTP has been sent.");
            return "redirect:/verify-otp?email=" + dto.getEmail();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(@RequestParam String email, Model model) {
        OtpVerificationDto dto = new OtpVerificationDto();
        dto.setEmail(email);
        model.addAttribute("otpVerificationDto", dto);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @Valid @ModelAttribute("otpVerificationDto") OtpVerificationDto dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "verify-otp";
        }

        boolean verified = authDao.verifyOtp(dto.getEmail(), dto.getOtp());
        if (verified) {
            return "redirect:/reset-password?email=" + dto.getEmail() + "&otp=" + dto.getOtp();
        } else {
            model.addAttribute("error", "Invalid or expired OTP. Please try again.");
            return "verify-otp";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(
            @RequestParam String email,
            @RequestParam String otp,
            Model model) {

        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setEmail(email);
        dto.setOtp(otp);
        model.addAttribute("resetPasswordDto", dto);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetPasswordDto") ResetPasswordDto dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "reset-password";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match.");
            return "reset-password";
        }

        try {
            boolean success = authDao.resetPassword(dto.getEmail(), dto.getOtp(), dto.getPassword());
            if (success) {
                model.addAttribute("success", "Password reset successful. Please login with your new password.");
                return "login";
            } else {
                model.addAttribute("error", "Invalid or expired OTP. Please request a new one.");
                return "forgot-password";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password";
        }
    }
}