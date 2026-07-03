package com.chhavi.pojo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "feedbacks")
public class Feedback {

    @Id
    private String id;

    private String userId;

    private String message;

    private String aiStatus;

    private LocalDateTime createdAt;

    public Feedback() {
    }

    public Feedback(String id, String userId, String message, String aiStatus, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.aiStatus = aiStatus;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAiStatus() {
        return aiStatus;
    }

    public void setAiStatus(String aiStatus) {
        this.aiStatus = aiStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Feedback [id=" + id + ", aiStatus=" + aiStatus + "]";
    }
}