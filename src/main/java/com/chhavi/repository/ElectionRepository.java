package com.chhavi.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.chhavi.pojo.Election;

@Repository
public interface ElectionRepository extends MongoRepository<Election, String> {
    Optional<Election> findByStatus(String status);
    boolean existsByStatus(String status);
}
