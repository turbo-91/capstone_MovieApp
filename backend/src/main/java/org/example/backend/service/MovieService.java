package org.example.backend.service;

import org.example.backend.exceptions.DatabaseException;
import org.example.backend.model.Movie;
import org.example.backend.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepo movieRepo;
    private final RestTemplate restTemplate;

    private final String tmdbApiKey;
    private final String netzkinoEnv;


    public MovieService(MovieRepo movieRepo, RestTemplate restTemplate, @Value("${TMDB_API_KEY}") String tmdbApiKey, @Value("${NETZKINO_ENV}") String netzkinoEnv ) {
        this.movieRepo = movieRepo;
        this.restTemplate = restTemplate;
        this.tmdbApiKey = tmdbApiKey;
        this.netzkinoEnv=netzkinoEnv;
    }

    // database interactions
    public List<Movie> getAllMovies() {
        System.out.println("Fetching all movies from database...");
        try {
            List<Movie> movies = movieRepo.findAll();
            System.out.println("Retrieved " + movies.size() + " movies from database.");
            return movies;
        } catch (Exception e) {
            System.out.println("Failed to fetch movies: " + e.getMessage());
            throw new DatabaseException("Failed to fetch movies.");
        }
    }

    public Movie getMovieBySlug(String slug) {
        System.out.println("Fetching movie by slug: " + slug);
        return movieRepo.findBySlug(slug)
                .orElseThrow(() -> {
                    System.out.println("Movie with slug " + slug + " not found!");
                    return new DatabaseException("Movie with slug " + slug + " not found.");
                });
    }

    public Movie saveMovie(Movie movie) {
        return movieRepo.save(movie);
    }

    public Movie updateMovie(Movie movie) {
        String slug = movie.slug();
        if (movieRepo.existsBySlug(slug)) {
            return movieRepo.save(movie);
        } else {
            throw new DatabaseException("Movie does not exist.");
        }
    }

    public void deleteMovie(String slug) {
        if (!movieRepo.existsBySlug(slug)) {
            throw new DatabaseException("Movie with slug " + slug + " does not exist.");
        }
        movieRepo.deleteBySlug(slug);
    }
}
