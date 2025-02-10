package org.example.backend.repo;

import org.example.backend.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepo extends MongoRepository<Movie, String> {

    boolean existsBySlug(String slug);

    void deleteBySlug(String slug);

    Optional<Movie> findBySlug(String slug);

    Optional<List<Movie>> findByDateFetchedContaining(LocalDate dateFetched);

    Optional<List<Movie>> findByQueriesContaining(String query);


}
