package com.chhavi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chhavi.pojo.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByUserIdAndElectionId(Long userId, Long electionId);
    long countByElectionId(Long electionId);
    long countByElectionIdAndCandidateId(Long electionId, Long candidateId);
    Optional<Vote> findByUserIdAndElectionId(Long userId, Long electionId);
}
