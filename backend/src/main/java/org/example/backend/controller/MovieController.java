package org.example.backend.controller;

import org.example.backend.model.Movie;
import org.example.backend.service.DailyMovieService;
import org.example.backend.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    private final MovieService movieService;
    private final DailyMovieService dailyMovieService;

    public MovieController(MovieService movieService, DailyMovieService dailyMovieService) {
        this.movieService = movieService;
        this.dailyMovieService = dailyMovieService;
    }

    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/{slug}")
    Movie getMovieBySlug(@PathVariable String slug) {
        try {
            return movieService.getMovieBySlug(slug);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping
    Movie createMovie(@RequestBody Movie movie)
    {
        return movieService.saveMovie(movie);
    }

    @PutMapping(path = {"{slug}"})
    Movie updateMovie(@PathVariable String slug, @RequestBody Movie movie) {
        if (!movie.slug().equals(slug)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The slug in the url does not match the request body's slug");
        }
        return movieService.updateMovie(movie);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Explicitly set the response status to 204
    void deleteWMovie(@PathVariable String slug) {
        try {
            movieService.deleteMovie(slug);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

//    @GetMapping("/search/{query}")
//    public ResponseEntity<List<Movie>> searchMovies(@PathVariable String query) {
//        System.out.println("CONTROLLER: Received search query: " + query);
//        List<Movie> movies = movieService.fetchAndStoreMovies(query);
//            return ResponseEntity.ok(movies);
//    }

    @GetMapping("/daily")
    public ResponseEntity<List<Movie>> getDailyMovies() {
        System.out.println("Received request for daily movies");
        try {
            List<Movie> movies = dailyMovieService.getMoviesOfTheDay(null); // Pass null to allow service to handle default
            System.out.println("Successfully retrieved " + movies.size() + " daily movies");
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            System.out.println("Error fetching daily movies: " + e.getMessage());
            e.printStackTrace(); // Print the full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());  // Return empty list instead of throwing exception
        }
    }

}


