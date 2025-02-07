package org.example.backend.repo;

import org.example.backend.model.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueryRepo extends MongoRepository<Query, String> {

    boolean existsByQuery(String query);

    void deleteByQuery(String query);

    Optional<Query> findByQuery(String query);
}