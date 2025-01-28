package org.example.backend.service;

import org.example.backend.model.Movie;
import org.example.backend.repo.MovieRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepo movieRepo;

    public MovieService(MovieRepo movieRepo) {
        this.movieRepo = movieRepo;
    }

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
