package com.chhavi.pojo;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "votes")
public class Vote {

    @Id
    private String id;

    private String userId;

    private String candidateId;

    private String electionId;

    private LocalDateTime voteTime;

    public Vote() {
    }

    public Vote(String id, String userId, String candidateId, String electionId, LocalDateTime voteTime) {
        this.id = id;
        this.userId = userId;
        this.candidateId = candidateId;
        this.electionId = electionId;
        this.voteTime = voteTime;
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

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getElectionId() {
        return electionId;
    }

    public void setElectionId(String electionId) {
        this.electionId = electionId;
    }

    public LocalDateTime getVoteTime() {
        return voteTime;
    }

    public void setVoteTime(LocalDateTime voteTime) {
        this.voteTime = voteTime;
    }

    @Override
    public String toString() {
        return "Vote [id=" + id + ", voteTime=" + voteTime + "]";
    }
}