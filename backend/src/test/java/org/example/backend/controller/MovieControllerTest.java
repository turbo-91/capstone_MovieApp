package org.example.backend.controller;

import org.example.backend.model.Movie;
import org.example.backend.service.MovieAPIService;
import org.example.backend.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MovieControllerTest {

    private MovieService movieService;
    private MovieAPIService movieAPIService;
    private MovieController movieController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        movieService = mock(MovieService.class);
        movieAPIService = mock(MovieAPIService.class);
        movieController = new MovieController(movieService, movieAPIService);
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void getAllMovies_ShouldReturnListOfMovies() throws Exception {
        // GIVEN
        List<Movie> movieList = List.of(
                new Movie(
                        "1",
                        101,
                        "slug-movie-1",
                        "Inception",
                        "2010",
                        "A thief who enters the dreams of others...",
                        "Christopher Nolan",
                        "Leonardo DiCaprio, Joseph Gordon-Levitt",
                        "https://example.com/netzkino1.jpg",
                        "https://example.com/netzkino1_small.jpg",
                        "https://example.com/imdb1.jpg",
                        List.of("Sci-Fi", "Thriller"),
                        List.of(LocalDate.now())
                ),
                new Movie(
                        "2",
                        102,
                        "slug-movie-2",
                        "The Dark Knight",
                        "2008",
                        "Batman battles the Joker in Gotham City...",
                        "Christopher Nolan",
                        "Christian Bale, Heath Ledger",
                        "https://example.com/netzkino2.jpg",
                        "https://example.com/netzkino2_small.jpg",
                        "https://example.com/imdb2.jpg",
                        List.of("Action", "Crime", "Drama"),
                        List.of(LocalDate.now())
                )
        );

        when(movieService.getAllMovies()).thenReturn(movieList);

        // WHEN & THEN
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk());

        verify(movieService).getAllMovies();
    }

    @Test
    void getMovieBySlug_ShouldReturnMovie_WhenSlugExists() throws Exception {
        // GIVEN
        String slug = "slug-movie-1";
        Movie expectedMovie = new Movie(
                "1",
                101,
                slug,
                "Inception",
                "2010",
                "A thief who enters the dreams of others...",
                "Christopher Nolan",
                "Leonardo DiCaprio, Joseph Gordon-Levitt",
                "https://example.com/netzkino1.jpg",
                "https://example.com/netzkino1_small.jpg",
                "https://example.com/imdb1.jpg",
                List.of("Sci-Fi", "Thriller"),
                List.of(LocalDate.now())
        );
        when(movieService.getMovieBySlug(slug)).thenReturn(expectedMovie);

        // WHEN & THEN
        mockMvc.perform(get("/api/movies/{slug}", slug))
                .andExpect(status().isOk());

        verify(movieService).getMovieBySlug(slug);
    }

    @Test
    void getMovieBySlug_ShouldReturnNotFound_WhenSlugDoesNotExist() throws Exception {
        // GIVEN
        String slug = "invalid-slug";
        when(movieService.getMovieBySlug(slug)).thenThrow(new IllegalArgumentException("Movie with slug " + slug + " not found."));

        // WHEN & THEN
        mockMvc.perform(get("/api/movies/{slug}", slug))
                .andExpect(status().isNotFound());

        verify(movieService).getMovieBySlug(slug);
    }

    @Test
    void deleteMovie_ShouldReturnNoContent_WhenSlugExists() throws Exception {
        // GIVEN
        String slug = "movie-to-delete";
        doNothing().when(movieService).deleteMovie(slug);

        // WHEN & THEN
        mockMvc.perform(delete("/api/movies/{slug}", slug))
                .andExpect(status().isNoContent());

        verify(movieService).deleteMovie(slug);
    }

    @Test
    void deleteMovie_ShouldReturnNotFound_WhenSlugDoesNotExist() throws Exception {
        // GIVEN
        String slug = "non-existent-movie";
        doThrow(new IllegalArgumentException("Movie with slug " + slug + " does not exist."))
                .when(movieService).deleteMovie(slug);

        // WHEN & THEN
        mockMvc.perform(delete("/api/movies/{slug}", slug))
                .andExpect(status().isNotFound());

        verify(movieService).deleteMovie(slug);
    }

    @Test
    void getDailyMovies_ShouldReturnListOfMovies() throws Exception {
        // GIVEN
        List<Movie> dailyMovies = List.of(
                new Movie(
                        "1",
                        101,
                        "slug-movie-1",
                        "Inception",
                        "2010",
                        "A thief who enters the dreams of others...",
                        "Christopher Nolan",
                        "Leonardo DiCaprio, Joseph Gordon-Levitt",
                        "https://example.com/netzkino1.jpg",
                        "https://example.com/netzkino1_small.jpg",
                        "https://example.com/imdb1.jpg",
                        List.of("Sci-Fi", "Thriller"),
                        List.of(LocalDate.now())
                ),
                new Movie(
                        "2",
                        102,
                        "slug-movie-2",
                        "The Dark Knight",
                        "2008",
                        "Batman battles the Joker in Gotham City...",
                        "Christopher Nolan",
                        "Christian Bale, Heath Ledger",
                        "https://example.com/netzkino2.jpg",
                        "https://example.com/netzkino2_small.jpg",
                        "https://example.com/imdb2.jpg",
                        List.of("Action", "Crime", "Drama"),
                        List.of(LocalDate.now())
                )
        );

        when(movieAPIService.getMoviesOfTheDay(any())).thenReturn(dailyMovies);

        // WHEN & THEN
        mockMvc.perform(get("/api/movies/daily"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(dailyMovies)));

        verify(movieAPIService).getMoviesOfTheDay(any());
    }

    @Test
    void getDailyMovies_ShouldReturnEmptyList_WhenNoMoviesAvailable() throws Exception {
        // GIVEN
        when(movieAPIService.getMoviesOfTheDay(any())).thenReturn(List.of());

        // WHEN & THEN
        mockMvc.perform(get("/api/movies/daily"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]")); // Expecting an empty JSON array

        verify(movieAPIService).getMoviesOfTheDay(any());
    }

    @Test
    void getMoviesOfTheDay_ShouldHandleExceptionAndReturnEmptyList() {
        // GIVEN
        List<String> queries = List.of("Inception");
        when(movieAPIService.getMoviesOfTheDay(queries)).thenThrow(new RuntimeException("Database error"));

        // WHEN
        ResponseEntity<List<Movie>> response = movieController.getDailyMovies();

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Verify HTTP 500 status
        assertEquals(List.of(), response.getBody()); // Verify the body contains an empty list
    }
}



