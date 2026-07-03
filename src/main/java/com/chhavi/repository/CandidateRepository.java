package com.chhavi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chhavi.pojo.Candidate;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
}
