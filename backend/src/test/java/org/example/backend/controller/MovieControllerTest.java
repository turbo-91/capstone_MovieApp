package org.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.model.Movie;
import org.example.backend.repo.MovieRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepo repo;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void clearDatabase() {
        repo.deleteAll();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("TMDB_API_KEY", () -> ("123"));
        registry.add("NETZKINO_ENV", () -> ("456"));
    }

    @Test
    void getAllMovies_shouldReturnListWithOneMovie_whenCalledWithFilledDB() throws Exception {
        // Clean database
        repo.deleteAll();

        //GIVEN
        Movie movie = new Movie(
                "herr-der-ringe-!",
                "Herr der Ringe - Die Gefährten",
                2007,
                "Der Film fängt an und hört auf.",
                "/hdrgefaehrtenphoto.jpg"
        );
        repo.save(movie);

        // Act & Assert
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(movie)))); // Use ObjectMapper to serialize the expected JSON
    }

    @Test
    void getMovieBySlug_shouldReturnMovie_whenMovieExists() throws Exception { // Clean database
        repo.deleteAll();

        //GIVEN
        Movie movie = new Movie(
                "herr-der-ringe-!!",
                "Herr der Ringe - Die zwei Türme",
                2009,
                "Der Film fängt wieder an und hört wieder auf.",
                "/hdrtuermephoto.jpg"
        );
        repo.save(movie);

        // Act & Assert
        mockMvc.perform(get("/api/movies/herr-der-ringe-!!"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(movie))); // Use ObjectMapper to serialize the expected JSON
    }

    @Test
    void getMovieBySlug_shouldReturn404_whenSlugDoesNotExist() throws Exception {
        // Clean database
        repo.deleteAll();

        // GIVEN
        String nonExistentSlug = "nonExistentId";

        // WHEN & THEN
        mockMvc.perform(get("/api/movies/" + nonExistentSlug))
                .andExpect(status().isNotFound());
    }


    @Test
    void createMovie_shouldPersistMovieAndReturnCreatedMovie_whenCalledWithValidPayload() throws Exception {
        // Clean database
        repo.deleteAll();

        // Arrange
        String requestBody = """
                {
                          "slug": "herr-der-ringe-!!",
                          "title": "Herr der Ringe - Die zwei Türme",
                          "year": 2009,
                          "overview": "Der Film fängt wieder an und hört wieder auf.",
                          "imgUrl": "/hdrtuermephoto.jpg"
                      }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("herr-der-ringe-!!"))
                .andExpect(jsonPath("$.title").value("Herr der Ringe - Die zwei Türme"))
                .andExpect(jsonPath("$.year").value(2009))
                .andExpect(jsonPath("$.overview").value("Der Film fängt wieder an und hört wieder auf."))
                .andExpect(jsonPath("$.imgUrl").value("/hdrtuermephoto.jpg"));

        // Verify the workout is saved in the database
        assertThat(repo.findAll()).hasSize(1);
        Movie savedMovie = repo.findAll().get(0);
        assertThat(savedMovie.slug()).isEqualTo("herr-der-ringe-!!");
        assertThat(savedMovie.title()).isEqualTo("Herr der Ringe - Die zwei Türme");
        assertThat(savedMovie.year()).isEqualTo(2009);
        assertThat(savedMovie.overview()).isEqualTo("Der Film fängt wieder an und hört wieder auf.");
        assertThat(savedMovie.imgUrl()).isEqualTo("/hdrtuermephoto.jpg");
    }

    @Test
    void createMovie_shouldReturnBadRequest_whenCalledWithInvalidPayload() throws Exception {
        // Clean database
        repo.deleteAll();

        // Arrange
        // Payload with missing required fields or invalid data
        String invalidRequestBody = """
    {
        "slug": "herr-der-ringe-!!",
        "title": "",
        "year": "invalid-year",
        "overview": null,
        "imgUrl": ""
    }
    """;

        // Act & Assert
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest()); // Expecting HTTP 400 Bad Request


        // Verify that no movie was persisted in the database
        assertThat(repo.findAll()).isEmpty();
    }


    @Test
    void expectSuccessfulPut() throws Exception {
        // Step 1: Create a new movie
        String saveResult = mockMvc.perform(
                        post("http://localhost:8080/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "slug": "original-slug",
                                      "title": "Original Title",
                                      "year": 2005,
                                      "overview": "Original overview.",
                                      "imgUrl": "/originalphoto.jpg"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                      "slug": "original-slug",
                      "title": "Original Title",
                      "year": 2005,
                      "overview": "Original overview.",
                      "imgUrl": "/originalphoto.jpg"
                    }
                    """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Step 2: Deserialize the result to get the slug
        Movie saveResultMovie = objectMapper.readValue(saveResult, Movie.class);
        String slug = saveResultMovie.slug();

        // Step 3: Update the movie
        mockMvc.perform(
                        put("http://localhost:8080/api/movies/" + slug)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "slug": "<SLUG>",
                                      "title": "Updated Title",
                                      "year": 2020,
                                      "overview": "Updated overview.",
                                      "imgUrl": "/updatedphoto.jpg"
                                    }
                                    """.replaceFirst("<SLUG>", slug))
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                      "slug": "<SLUG>",
                      "title": "Updated Title",
                      "year": 2020,
                      "overview": "Updated overview.",
                      "imgUrl": "/updatedphoto.jpg"
                    }
                    """.replaceFirst("<SLUG>", slug)));
    }

    @Test
    void deleteMovie_shouldDeleteMovieAndReturnNoContent_whenSlugExists() throws Exception {
        // GIVEN
        Movie existingMovie = new Movie(
                "herr-der-ringe-gefährten",
                "Herr der Ringe - Die Gefährten",
                2001,
                "Eine epische Reise beginnt.",
                "/hdrgefaehrtenphoto.jpg"
        );
        repo.save(existingMovie);

        // WHEN & THEN
        mockMvc.perform(delete("/api/movies/herr-der-ringe-gefährten"))
                .andExpect(status().isNoContent());

        // Verify that the movie is deleted
        assertThat(repo.findBySlug("herr-der-ringe-gefährten")).isEmpty();
    }

    @Test
    void deleteMovie_shouldReturnNotFound_whenSlugDoesNotExist() throws Exception {
        // GIVEN
        String nonExistentSlug = "herr-der-ringe-unbekannt";

        // WHEN & THEN
        mockMvc.perform(delete("/api/movies/" + nonExistentSlug))
                .andExpect(status().isNotFound());
    }

}