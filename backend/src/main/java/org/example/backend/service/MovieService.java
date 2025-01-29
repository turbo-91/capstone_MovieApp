package org.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.backend.model.Movie;
import org.example.backend.model.NetzkinoResponse;
import org.example.backend.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepo movieRepo;
    private final RestTemplate restTemplate;

    @Value("${TMDB_API_KEY}")
    private String tmdbApiKey;

    @Value("${NETZKINO_ENV}")
    private String netzkinoEnv;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/find/";
    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w500";

    public MovieService(MovieRepo movieRepo, RestTemplate restTemplate) {
        this.movieRepo = movieRepo;
        this.restTemplate = restTemplate;
    }

    public List<Movie> fetchAndStoreMovies() {
        System.out.println("Starting movie fetching process...");

        ResponseEntity<NetzkinoResponse[]> response = restTemplate.getForEntity(API_1_URL, NetzkinoResponse[].class);
        NetzkinoResponse[] netzkinoResponses = response.getBody();


    };

    public List<Movie> getAllMovies() {
        try {
            List<Movie> movies = movieRepo.findAll();
            System.out.println("Service: Fetched all movies: " + movies);
            return movies;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch movies from the database.", e);
        }
    }
    public Movie getMovieBySlug(String slug) {
        return movieRepo.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Movie with slug " + slug + " not found."));
    }

    public Movie saveMovie(Movie movie) {
        return movieRepo.save(movie);
    }

    public Movie updateMovie(Movie movie) {
        if (movieRepo.existsBySlug(movie.slug())) {
            return movieRepo.save(movie);
        } else {
            throw new IllegalArgumentException("Movie with slug " + movie.slug() + " does not exist.");
        }
    }

    public void deleteMovie(String slug) {
        if (!movieRepo.existsBySlug(slug)) {
            throw new IllegalArgumentException("Movie with slug " + slug + " does not exist.");
        }
        movieRepo.deleteBySlug(slug);
    }

}
