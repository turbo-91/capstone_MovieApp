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
            System.out.println("Service: Fetched all workouts: " + movies);
            return movies;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch workouts from the database.", e);
        }
    }
    public Movie getMovieById(String id) {
        return movieRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workout with ID " + id + " not found."));
    }

    public Movie saveMovie(Movie movie) {
        return movieRepo.save(movie);
    }

    public Movie updateMovie(Movie movie) {
        if (movieRepo.existsById(movie.slug())) {
            return movieRepo.save(movie);
        } else {
            throw new IllegalArgumentException("Workout with ID " + movie.slug() + " does not exist.");
        }
    }

    public void deleteWorkout(String id) {
        if (!movieRepo.existsById(id)) {
            throw new IllegalArgumentException("Workout with ID " + id + " does not exist.");
        }
        movieRepo.deleteById(id);
    }

}
