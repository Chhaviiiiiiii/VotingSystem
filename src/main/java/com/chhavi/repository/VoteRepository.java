package com.chhavi.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.chhavi.pojo.Vote;

@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
    boolean existsByUserIdAndElectionId(String userId, String electionId);
    long countByElectionId(String electionId);
    long countByElectionIdAndCandidateId(String electionId, String candidateId);
    Optional<Vote> findByUserIdAndElectionId(String userId, String electionId);
}
