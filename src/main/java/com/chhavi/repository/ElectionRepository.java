package com.chhavi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chhavi.pojo.Election;

@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {
    Optional<Election> findByStatus(String status);
    boolean existsByStatus(String status);
}
