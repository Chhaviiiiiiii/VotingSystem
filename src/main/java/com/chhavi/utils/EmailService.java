package com.chhavi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.chhavi.pojo.User;
import com.chhavi.pojo.Election;
import com.chhavi.pojo.Candidate;
import com.chhavi.pojo.PendingRegistration;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.dev-fallback:false}")
    private boolean devFallback;

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationOtpEmail(PendingRegistration pending) {
        sendVerificationOtpEmail(pending.getEmail(), pending.getFullName(), pending.getOtp());
    }

    public void sendVerificationOtpEmail(User user) {
        sendVerificationOtpEmail(user.getEmail(), user.getFullName(), user.getVerificationOtp());
    }

    public void sendVerificationOtpEmail(String email, String fullName, String otp) {
        String subject = "Verify Your Email - VOTE INDIA";
        String message = "VOTE INDIA\n"
                + "Digital Voting Portal\n\n"
                + "Verify your email address\n\n"
                + "Use the verification code below to complete your registration.\n\n"
                + "Verification Code: " + otp + "\n\n"
                + "This code expires in 10 minutes.\n"
                + "Do not share this code with anyone.\n\n"
                + "Best regards,\nVOTE INDIA Team";

        sendEmail(email, subject, message);
    }

    public void sendOtpEmail(String email, String otp) {
        String subject = "Password Reset OTP - AI Online Voting System";
        String message = "Your OTP for password reset is: " + otp + "\n\n"
                + "This OTP is valid for 10 minutes.\n\n"
                + "If you did not request a password reset, please ignore this email.";

        sendEmail(email, subject, message);
    }

    public void sendElectionNotificationEmail(User user, Election election) {
        String subject = "New Election Activated - AI Online Voting System";
        String message = "Dear " + user.getFullName() + ",\n\n"
                + "A new election has been activated: " + election.getTitle() + "\n\n"
                + "Description: " + election.getDescription() + "\n"
                + "Start Date: " + election.getStartDate() + "\n"
                + "End Date: " + election.getEndDate() + "\n\n"
                + "Please log in to your account and cast your vote.\n\n"
                + "Best regards,\nOnline Voting System Team";

        sendEmail(user.getEmail(), subject, message);
    }

    public void sendVoteConfirmationEmail(User user, Election election, Candidate candidate) {
        String subject = "Vote Recorded Successfully - AI Online Voting System";
        String message = "Dear " + user.getFullName() + ",\n\n"
                + "Your vote for the election '" + election.getTitle() + "' has been successfully recorded.\n"
                + "Candidate: " + candidate.getName() + " (" + candidate.getParty() + ")\n"
                + "Vote Time: " + java.time.LocalDateTime.now() + "\n\n"
                + "Thank you for participating in the democratic process!\n\n"
                + "Best regards,\nOnline Voting System Team";

        sendEmail(user.getEmail(), subject, message);
    }

    public void sendElectionResultEmail(User user, Election election, List<Map<String, Object>> candidateResults) {
        String subject = "Election Results Published - AI Online Voting System";
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(user.getFullName()).append(",\n\n");
        sb.append("The results for the election '").append(election.getTitle()).append("' have been published.\n\n");
        sb.append("Scoreboard:\n");
        sb.append("--------------------------------------------------\n");
        for (Map<String, Object> res : candidateResults) {
            sb.append(res.get("name")).append(" (").append(res.get("party")).append("): ")
              .append(res.get("votes")).append(" votes (")
              .append(String.format("%.2f", (Double) res.get("percentage"))).append("%)\n");
        }
        sb.append("--------------------------------------------------\n\n");
        sb.append("Thank you for participating.\n\n");
        sb.append("Best regards,\nOnline Voting System Team");

        sendEmail(user.getEmail(), subject, sb.toString());
    }

    private void sendEmail(String to, String subject, String text) {
        if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
            sendEmailViaBrevo(to, subject, text);
            return;
        }

        if (mailUsername == null || mailUsername.trim().isEmpty()) {
            if (devFallback) {
                logger.info("====== DEV MAIL SENDER (No credentials configured) ======");
                logger.info("To: {}", to);
                logger.info("Subject: {}", subject);
                logger.info("Body:\n{}", text);
                logger.info("========================================================");
                return;
            } else {
                throw new IllegalStateException("SMTP username is not configured and dev-fallback is disabled.");
            }
        }

        int maxAttempts = 3;
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxAttempts) {
            attempt++;
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(mailUsername);
                mailMessage.setTo(to);
                mailMessage.setSubject(subject);
                mailMessage.setText(text);
                mailSender.send(mailMessage);
                logger.info("Email sent successfully to {} on attempt {}", to, attempt);
                return;
            } catch (Exception e) {
                lastException = e;
                logger.warn("Failed to send email to {} on attempt {} of {}. Error: {}", to, attempt, maxAttempts, e.getMessage());
                if (attempt < maxAttempts) {
                    long backoffMs = (long) Math.pow(2, attempt) * 1000;
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        logger.error("Failed to send email to {} after {} attempts. Error details logged on server.", to, maxAttempts, lastException);
        if (devFallback) {
            logger.info("====== DEV MAIL SENDER FALLBACK ======");
            logger.info("To: {}", to);
            logger.info("Subject: {}", subject);
            logger.info("Body:\n{}", text);
            logger.info("======================================");
        } else {
            throw new RuntimeException("We couldn't send the verification email at the moment. Please try again in a few minutes.");
        }
    }

    private void sendEmailViaBrevo(String to, String subject, String text) {
        logger.info("Attempting to send email via Brevo HTTP API to {}", to);
        try {
            String senderEmail = (mailUsername != null && !mailUsername.trim().isEmpty()) ? mailUsername : "noreply@voteindia.com";
            String payload = "{"
                    + "\"sender\":{\"name\":\"VOTE INDIA\",\"email\":\"" + escapeJson(senderEmail) + "\"},"
                    + "\"to\":[{\"email\":\"" + escapeJson(to) + "\"}],"
                    + "\"subject\":\"" + escapeJson(subject) + "\","
                    + "\"textContent\":\"" + escapeJson(text) + "\""
                    + "}";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", brevoApiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload, java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Email sent successfully to {} via Brevo API (Status: {})", to, response.statusCode());
            } else {
                logger.error("Failed to send email via Brevo. Status: {}, Response: {}", response.statusCode(), response.body());
                throw new RuntimeException("Brevo API returned status code " + response.statusCode());
            }
        } catch (Exception e) {
            logger.error("Error sending email via Brevo: {}", e.getMessage(), e);
            throw new RuntimeException("We couldn't send the verification email at the moment. Please try again in a few minutes.", e);
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}