package org.example.backend.controller;

import org.example.backend.model.Movie;
import org.example.backend.repo.MovieRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.util.AssertionErrors.assertEquals;



@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = {
        "TMDB_API_KEY=dummy-api-key",
        "NETZKINO_ENV=test-environment"
})
class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MovieRepo movieRepo;

    @DirtiesContext
    @Test
    void getAllMovies_shouldReturnEmptyList_whenRepositoryIsEmpty() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/movies"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[]"));
    }

    @DirtiesContext
    @Test
    void getAllMovies_shouldReturnListWithOneObject_whenOneObjectWasSavedInRepository() throws Exception {
        Movie movie = new Movie(
                "1",
                12345,
                "test-movie",
                "Test Movie",
                "2024",
                "A test movie description",
                "Test Director",
                "Test Stars",
                "test-netzkino-img",
                "test-netzkino-img-small",
                "test-imdb-img",
                List.of("test-query"),
                List.of(LocalDate.now())
        );
        movieRepo.save(movie);

        mvc.perform(MockMvcRequestBuilders.get("/api/movies"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        [
                         {
                             "id": "1",
                             "netzkinoId": 12345,
                             "slug": "test-movie",
                             "title": "Test Movie",
                             "year": "2024",
                             "overview": "A test movie description",
                             "regisseur": "Test Director",
                             "stars": "Test Stars",
                             "imgNetzkino": "test-netzkino-img",
                             "imgNetzkinoSmall": "test-netzkino-img-small",
                             "imgImdb": "test-imdb-img",
                             "queries": ["test-query"],
                             "dateFetched": ["%s"]
                         }
                        ]
                        """.formatted(LocalDate.now())));
    }

    @DirtiesContext
    @Test
    void getMovieBySlug_shouldReturnMovie_whenMovieExists() throws Exception {
        // Arrange: Create and save a test movie
        Movie movie = new Movie(
                "1",
                12345,
                "test-movie",
                "Test Movie",
                "2024",
                "A test movie description",
                "Test Director",
                "Test Stars",
                "test-netzkino-img",
                "test-netzkino-img-small",
                "test-imdb-img",
                List.of("test-query"),
                List.of(LocalDate.now())
        );
        movieRepo.save(movie);

        // Act & Assert: Make a GET request and check response
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/test-movie"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                             "id": "1",
                             "netzkinoId": 12345,
                             "slug": "test-movie",
                             "title": "Test Movie",
                             "year": "2024",
                             "overview": "A test movie description",
                             "regisseur": "Test Director",
                             "stars": "Test Stars",
                             "imgNetzkino": "test-netzkino-img",
                             "imgNetzkinoSmall": "test-netzkino-img-small",
                             "imgImdb": "test-imdb-img",
                             "queries": ["test-query"],
                             "dateFetched": ["%s"]
                        }
                        """.formatted(LocalDate.now())));
    }

    @DirtiesContext
    @Test
    void getMovieBySlug_shouldReturnNotFound_whenMovieDoesNotExist() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/non-existent-movie"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Movie with slug non-existent-movie not found."));
    }

    @DirtiesContext
    @Test
    void createMovie_shouldReturnCreatedMovie_whenValidRequestIsSent() throws Exception {
        // Arrange: Define the movie JSON payload
        String movieJson = """
        {
            "id": "1",
            "netzkinoId": 12345,
            "slug": "test-movie",
            "title": "Test Movie",
            "year": "2024",
            "overview": "A test movie description",
            "regisseur": "Test Director",
            "stars": "Test Stars",
            "imgNetzkino": "test-netzkino-img",
            "imgNetzkinoSmall": "test-netzkino-img-small",
            "imgImdb": "test-imdb-img",
            "queries": ["test-query"],
            "dateFetched": ["%s"]
        }
        """.formatted(LocalDate.now());

        // Act & Assert: Send a POST request and verify response
        mvc.perform(MockMvcRequestBuilders.post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson).with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Assuming successful creation returns 200 OK
                .andExpect(MockMvcResultMatchers.content().json(movieJson));

        // Verify: Check if movie was actually saved in the database
        assertTrue(movieRepo.existsBySlug("test-movie"));
    }

    @DirtiesContext
    @Test
    void updateMovie_shouldReturnUpdatedMovie_whenMovieExists() throws Exception {
        // Arrange: Save a test movie
        Movie existingMovie = new Movie(
                "1",
                12345,
                "test-movie",
                "Test Movie",
                "2024",
                "A test movie description",
                "Test Director",
                "Test Stars",
                "test-netzkino-img",
                "test-netzkino-img-small",
                "test-imdb-img",
                List.of("test-query"),
                List.of(LocalDate.now())
        );
        movieRepo.save(existingMovie);

        // Define updated movie JSON payload
        String updatedMovieJson = """
        {
            "id": "1",
            "netzkinoId": 12345,
            "slug": "test-movie",
            "title": "Updated Movie Title",
            "year": "2025",
            "overview": "Updated description",
            "regisseur": "Updated Director",
            "stars": "Updated Stars",
            "imgNetzkino": "updated-netzkino-img",
            "imgNetzkinoSmall": "updated-netzkino-img-small",
            "imgImdb": "updated-imdb-img",
            "queries": ["updated-query"],
            "dateFetched": ["%s"]
        }
        """.formatted(LocalDate.now());

        // Act & Assert: Send a PUT request and verify response
        mvc.perform(MockMvcRequestBuilders.put("/api/movies/test-movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedMovieJson).with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(updatedMovieJson));

        // Verify: Check if movie was actually updated in the database
        Movie updatedMovie = movieRepo.findBySlug("test-movie").orElseThrow();
        assertEquals("Updated Movie Title", updatedMovie.title(), "Updated Movie Title");
        assertEquals("Updated Director", updatedMovie.regisseur(), "Updated Director");
    }

    @DirtiesContext
    @Test
    void deleteMovie_shouldReturnNoContent_whenMovieExists() throws Exception {
        // Arrange: Save a test movie
        Movie existingMovie = new Movie(
                "1",
                12345,
                "test-movie",
                "Test Movie",
                "2024",
                "A test movie description",
                "Test Director",
                "Test Stars",
                "test-netzkino-img",
                "test-netzkino-img-small",
                "test-imdb-img",
                List.of("test-query"),
                List.of(LocalDate.now())
        );
        movieRepo.save(existingMovie);

        // Act & Assert: Send DELETE request and expect 204 No Content
        mvc.perform(MockMvcRequestBuilders.delete("/api/movies/test-movie").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // Verify: Check that the movie is no longer in the database
        assertFalse(movieRepo.existsBySlug("test-movie"));
    }

    @DirtiesContext
    @Test
    void getDailyMovies_shouldReturnMoviesList_whenMoviesExist() throws Exception {
        // Arrange: Save test movies
        Movie movie1 = new Movie(
                "1",
                12345,
                "daily-movie-1",
                "Daily Movie 1",
                "2024",
                "Description 1",
                "Director 1",
                "Stars 1",
                "img1-netzkino",
                "img1-netzkino-small",
                "img1-imdb",
                List.of("query1"),
                List.of(LocalDate.now())
        );
        Movie movie2 = new Movie(
                "2",
                67890,
                "daily-movie-2",
                "Daily Movie 2",
                "2025",
                "Description 2",
                "Director 2",
                "Stars 2",
                "img2-netzkino",
                "img2-netzkino-small",
                "img2-imdb",
                List.of("query2"),
                List.of(LocalDate.now())
        );
        movieRepo.save(movie1);
        movieRepo.save(movie2);

        // Act & Assert: Expect a JSON array with two movies
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/daily"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
            [
                {
                    "id": "1",
                    "netzkinoId": 12345,
                    "slug": "daily-movie-1",
                    "title": "Daily Movie 1",
                    "year": "2024",
                    "overview": "Description 1",
                    "regisseur": "Director 1",
                    "stars": "Stars 1",
                    "imgNetzkino": "img1-netzkino",
                    "imgNetzkinoSmall": "img1-netzkino-small",
                    "imgImdb": "img1-imdb",
                    "queries": ["query1"],
                    "dateFetched": ["%s"]
                },
                {
                    "id": "2",
                    "netzkinoId": 67890,
                    "slug": "daily-movie-2",
                    "title": "Daily Movie 2",
                    "year": "2025",
                    "overview": "Description 2",
                    "regisseur": "Director 2",
                    "stars": "Stars 2",
                    "imgNetzkino": "img2-netzkino",
                    "imgNetzkinoSmall": "img2-netzkino-small",
                    "imgImdb": "img2-imdb",
                    "queries": ["query2"],
                    "dateFetched": ["%s"]
                }
            ]
        """.formatted(LocalDate.now(), LocalDate.now())));
    }

}
