package com.chhavi.pojo;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "election_id"})
})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "election_id")
    private Election election;

    private LocalDateTime voteTime;

    public Vote() {
    }

    public Vote(Long id, User user, Candidate candidate, Election election, LocalDateTime voteTime) {
        this.id = id;
        this.user = user;
        this.candidate = candidate;
        this.election = election;
        this.voteTime = voteTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Election getElection() {
        return election;
    }

    public void setElection(Election election) {
        this.election = election;
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