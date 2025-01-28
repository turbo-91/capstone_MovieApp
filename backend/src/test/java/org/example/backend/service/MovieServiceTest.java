package org.example.backend.service;

import org.example.backend.model.Movie;
import org.example.backend.repo.MovieRepo;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MovieServiceTest {

    private final MovieRepo repo= mock(MovieRepo.class);

    @Test
    void getAllMovies_ShouldReturnListOfMovies_whenCalled() {
        //GIVEN
        Movie movie1 = new Movie(
                "herr-der-ringe-!",
                "Herr der Ringe - Die Gefährten",
                2007,
                "Der Film fängt an und hört auf.",
                "/hdrgefaehrtenphoto.jpg"
        );

        Movie movie2 = new Movie(
                "herr-der-ringe-!",
                "Herr der Ringe - Die zwei Türme",
                2009,
                "Der Film fängt wieder an und hört wieder auf.",
                "/hdrtuermephoto.jpg"
        );

        MovieService movieService = new MovieService(repo);
        List<Movie> movieList = List.of(movie1, movie2);

        when(repo.findAll()).thenReturn(movieList); // Mock the repository to return the list

        List<Movie> expected = movieList;

        //WHEN
        List<Movie> actual = movieService.getAllMovies();

        //THEN
        assertEquals(expected, actual); // Verify the list matches
        verify(repo).findAll(); // Verify findAll was called once
    }

    @Test
    void getMovieById_ShouldReturnMovie_whenSlugExists() {
        // GIVEN
        String slug = "herr-der-ringe-!";
        Movie expectedMovie = new Movie(
                slug,
                "Herr der Ringe - Die Gefährten",
                2007,
                "Der Film fängt an und hört auf.",
                "/hdrgefaehrtenphoto.jpg"
        );

        when(repo.findBySlug(slug)).thenReturn(java.util.Optional.of(expectedMovie)); // Mock repository to return the movie

        MovieService movieService = new MovieService(repo);

        // WHEN
        Movie actualMovie = movieService.getMovieBySlug(slug);

        // THEN
        assertEquals(expectedMovie, actualMovie); // Verify the returned movie matches the expected one
        verify(repo).findBySlug(slug); // Verify findById was called with the correct movie
    }

    @Test
    void getMovieBySlug_ShouldThrowException_whenSlugDoesNotExist() {
        // GIVEN
        String nonExistentSlug = "nonExistentSlug";

        when(repo.findBySlug(nonExistentSlug)).thenReturn(java.util.Optional.empty()); // Mock repository to return empty

        MovieService movieService = new MovieService(repo);

        // WHEN & THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            movieService.getMovieBySlug(nonExistentSlug);
        });

        assertEquals("Movie with slug " + nonExistentSlug + " not found.", exception.getMessage());
        verify(repo).findBySlug(nonExistentSlug); // Verify findById was called with the correct ID
    }

    @Test
    void saveMovie_shouldAddMovie_whenCalledWithMovie() {
        // GIVEN
        Movie inputMovie = new Movie(
                "herr-der-ringe-!",
                "Herr der Ringe - Die Gefährten",
                2007,
                "Der Film fängt an und hört auf.",
                "/hdrgefaehrtenphoto.jpg"
        );
        Movie expectedMovie = inputMovie.withSlug("herr-der-ringe-!");

        when(repo.save(expectedMovie)).thenReturn(expectedMovie);

        MovieService movieService = new MovieService(repo);

        // WHEN
        Movie actual = movieService.saveMovie(inputMovie);

        // THEN
        assertEquals(expectedMovie, actual);
        verify(repo).save(expectedMovie);
    }

    @Test
    void updateWorkout_ShouldUpdateWorkout_whenSlugExists() {
        // GIVEN
        String slug = "herr-der-ringe-!";
       Movie existingMovie = new Movie(
               slug,
               "Herr der Ringe - Die Gefährten",
               2007,
               "Der Film fängt an und hört auf.",
               "/hdrgefaehrtenphoto.jpg"
       );

        Movie updatedMovie = new Movie(
              slug,
                "Die Gefährten",
                2007,
                "Der Film fängt an und hört auf.",
                "/hdrgefaehrtenphoto.jpg"
        );

        when(repo.existsBySlug(slug)).thenReturn(true); // Mock repository to indicate the workout exists
        when(repo.save(updatedMovie)).thenReturn(updatedMovie); // Mock save to return the updated workout

        MovieService movieService = new MovieService(repo);

        // WHEN
        Movie actual = movieService.updateMovie(updatedMovie);

        // THEN
        assertEquals(updatedMovie, actual); // Verify the updated workout is returned
        verify(repo).existsBySlug(slug); // Verify existsById was called with the correct ID
        verify(repo).save(updatedMovie); // Verify save was called with the updated workout
    }

    @Test
    void updateMovie_ShouldThrowException_whenSlugDoesNotExist() {
        // GIVEN
        String nonExistentSlug= "nonExistentSlug";
        Movie movieToUpdate = new Movie(
                nonExistentSlug,
                "Die Gefährten",
                2007,
                "Der Film fängt an und hört auf.",
                "/hdrgefaehrtenphoto.jpg"
        );

        when(repo.existsBySlug(nonExistentSlug)).thenReturn(false); // Mock repository to indicate the workout does not exist

        MovieService movieService = new MovieService(repo);

        // WHEN & THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            movieService.updateMovie(movieToUpdate);
        });

        assertEquals("Movie with slug " + nonExistentSlug + " does not exist.", exception.getMessage());
        verify(repo).existsBySlug(nonExistentSlug); // Verify existsBySlug was called with the correct Slug
        verify(repo, never()).save(any()); // Verify save was never called
    }

    @Test
    void deleteMovie_ShouldDeleteMovie_whenSlugExists() {
        // GIVEN
        String slug = "movie1";

        when(repo.existsBySlug(slug)).thenReturn(true); // Mock repository to indicate the workout exists

       MovieService movieService = new MovieService(repo);

        // WHEN
        movieService.deleteMovie(slug);

        // THEN
        verify(repo).existsBySlug(slug); // Verify existsBySlug was called with the correct Slug
        verify(repo).deleteBySlug(slug); // Verify deleteBySlug was called with the correct Slug
    }

    @Test
    void deleteMovie_ShouldThrowException_whenSlugDoesNotExist() {
        // GIVEN
        String nonExistentSlug = "nonExistentSlug";

        when(repo.existsBySlug(nonExistentSlug)).thenReturn(false); // Mock repository to indicate the movie does not exist

        MovieService movieService = new MovieService(repo);

        // WHEN & THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            movieService.deleteMovie(nonExistentSlug);
        });

        assertEquals("Movie with slug " + nonExistentSlug + " does not exist.", exception.getMessage());
        verify(repo).existsBySlug(nonExistentSlug); // Verify existsBySlug was called with the correct Slug
        verify(repo, never()).deleteBySlug(any()); // Verify deleteBySlug was never called
    }

}