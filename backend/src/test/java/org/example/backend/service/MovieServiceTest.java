package org.example.backend.service;

import org.example.backend.model.Movie;
import org.example.backend.repo.MovieRepo;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MovieServiceTest {

    private final MovieRepo repo= mock(MovieRepo.class);

    @Test
    void getAllMovies_ShouldReturnListOfToDos_whenCalled() {
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

}