//package org.example.backend.controller;
//
//import org.example.backend.model.Movie;
//import org.example.backend.service.MovieService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//class MovieControllerTest {
//
//    private MovieService movieService;
//    private MovieController movieController;
//    private MockMvc mockMvc;
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setUp() {
//        movieService = mock(MovieService.class);
//        movieController = new MovieController(movieService);
//        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
//        objectMapper = new ObjectMapper();
//    }
//
//    @Test
//    void getAllMovies_ShouldReturnListOfMovies() throws Exception {
//        // GIVEN
//        List<Movie> movieList = List.of(
//                new Movie("lotr1", "Lord of the Rings: Fellowship", 2001, "First movie", "/image1.jpg"),
//                new Movie("lotr2", "Lord of the Rings: Two Towers", 2002, "Second movie", "/image2.jpg")
//        );
//        when(movieService.getAllMovies()).thenReturn(movieList);
//
//        // WHEN & THEN
//        mockMvc.perform(get("/api/movies"))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(movieList)));
//
//        verify(movieService).getAllMovies();
//    }
//
//    @Test
//    void getMovieBySlug_ShouldReturnMovie_WhenSlugExists() throws Exception {
//        // GIVEN
//        String slug = "lotr1";
//        Movie expectedMovie = new Movie(slug, "Lord of the Rings: Fellowship", 2001, "First movie", "/image1.jpg");
//        when(movieService.getMovieBySlug(slug)).thenReturn(expectedMovie);
//
//        // WHEN & THEN
//        mockMvc.perform(get("/api/movies/{slug}", slug))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(expectedMovie)));
//
//        verify(movieService).getMovieBySlug(slug);
//    }
//
//    @Test
//    void getMovieBySlug_ShouldReturnNotFound_WhenSlugDoesNotExist() throws Exception {
//        // GIVEN
//        String slug = "invalid-slug";
//        when(movieService.getMovieBySlug(slug)).thenThrow(new IllegalArgumentException("Movie with slug " + slug + " not found."));
//
//        // WHEN & THEN
//        mockMvc.perform(get("/api/movies/{slug}", slug))
//                .andExpect(status().isNotFound());
//
//        verify(movieService).getMovieBySlug(slug);
//    }
//
//    @Test
//    void createMovie_ShouldReturnCreatedMovie() throws Exception {
//        // GIVEN
//        Movie newMovie = new Movie("new-movie", "New Movie", 2023, "A new story.", "/new-image.jpg");
//        when(movieService.saveMovie(any(Movie.class))).thenReturn(newMovie);
//
//        // WHEN & THEN
//        mockMvc.perform(post("/api/movies")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(newMovie)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(newMovie)));
//
//        verify(movieService).saveMovie(any(Movie.class));
//    }
//
//    @Test
//    void updateMovie_ShouldReturnUpdatedMovie_WhenSlugMatches() throws Exception {
//        // GIVEN
//        String slug = "existing-movie";
//        Movie updatedMovie = new Movie(slug, "Updated Movie", 2023, "Updated description.", "/updated.jpg");
//
//        when(movieService.updateMovie(any(Movie.class))).thenReturn(updatedMovie);
//
//        // WHEN & THEN
//        mockMvc.perform(put("/api/movies/{slug}", slug)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedMovie)))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(updatedMovie)));
//
//        verify(movieService).updateMovie(any(Movie.class));
//    }
//
//    @Test
//    void updateMovie_ShouldReturnBadRequest_WhenSlugDoesNotMatch() throws Exception {
//        // GIVEN
//        String urlSlug = "wrong-slug";
//        Movie movieWithDifferentSlug = new Movie("different-slug", "Updated Movie", 2023, "Updated description.", "/updated.jpg");
//
//        // WHEN & THEN
//        mockMvc.perform(put("/api/movies/{slug}", urlSlug)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(movieWithDifferentSlug)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void deleteMovie_ShouldReturnNoContent_WhenSlugExists() throws Exception {
//        // GIVEN
//        String slug = "movie-to-delete";
//        doNothing().when(movieService).deleteMovie(slug);
//
//        // WHEN & THEN
//        mockMvc.perform(delete("/api/movies/{slug}", slug))
//                .andExpect(status().isNoContent());
//
//        verify(movieService).deleteMovie(slug);
//    }
//
//    @Test
//    void deleteMovie_ShouldReturnNotFound_WhenSlugDoesNotExist() throws Exception {
//        // GIVEN
//        String slug = "non-existent-movie";
//        doThrow(new IllegalArgumentException("Movie with slug " + slug + " does not exist."))
//                .when(movieService).deleteMovie(slug);
//
//        // WHEN & THEN
//        mockMvc.perform(delete("/api/movies/{slug}", slug))
//                .andExpect(status().isNotFound());
//
//        verify(movieService).deleteMovie(slug);
//    }
//
//    @Test
//    void searchMovies_ShouldReturnMovies_WhenQueryIsValid() throws Exception {
//        // GIVEN
//        String query = "Lord of the Rings";
//        List<Movie> expectedMovies = List.of(
//                new Movie("lotr1", "Lord of the Rings: Fellowship", 2001, "First movie", "/image1.jpg"),
//                new Movie("lotr2", "Lord of the Rings: Two Towers", 2002, "Second movie", "/image2.jpg")
//        );
//        when(movieService.fetchAndStoreMovies(query)).thenReturn(expectedMovies);
//
//        // WHEN & THEN
//        mockMvc.perform(get("/api/movies/search/{query}", query))
//                .andExpect(status().isOk())
//                .andExpect(content().json(objectMapper.writeValueAsString(expectedMovies)));
//
//        verify(movieService).fetchAndStoreMovies(query);
//    }
//
//    @Test
//    void searchMovies_ShouldReturnEmptyList_WhenNoResultsFound() throws Exception {
//        // GIVEN
//        String query = "Non-existent Movie";
//        when(movieService.fetchAndStoreMovies(query)).thenReturn(List.of());
//
//        // WHEN & THEN
//        mockMvc.perform(get("/api/movies/search/{query}", query))
//                .andExpect(status().isOk())
//                .andExpect(content().json("[]"));
//
//        verify(movieService).fetchAndStoreMovies(query);
//    }
//}
