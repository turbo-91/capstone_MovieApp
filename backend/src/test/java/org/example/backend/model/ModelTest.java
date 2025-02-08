package org.example.backend.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    @Test
    void testMovieCreation() {
        LocalDate date1 = LocalDate.of(2023, 2, 15);
        LocalDate date2 = LocalDate.of(2023, 3, 10);

        Movie movie = new Movie(
                "1",
                1001,
                "movie-slug",
                "Movie Title",
                "2022",
                "An overview of the movie.",
                "Director Name",
                "Star1, Star2",
                "img_netzkino.jpg",
                "img_netzkino_small.jpg",
                "img_imdb.jpg",
                List.of("query1", "query2"),
                List.of(date1, date2)
        );

        assertNotNull(movie);
        assertEquals("1", movie.id());
        assertEquals(1001, movie.netzkinoId());
        assertEquals("movie-slug", movie.slug());
        assertEquals("Movie Title", movie.title());
        assertEquals("2022", movie.year());
        assertEquals("An overview of the movie.", movie.overview());
        assertEquals("Director Name", movie.regisseur());
        assertEquals("Star1, Star2", movie.stars());
        assertEquals("img_netzkino.jpg", movie.imgNetzkino());
        assertEquals("img_netzkino_small.jpg", movie.imgNetzkinoSmall());
        assertEquals("img_imdb.jpg", movie.imgImdb());
        assertEquals(List.of("query1", "query2"), movie.queries());
        assertEquals(List.of(date1, date2), movie.dateFetched());
    }

    @Test
    void testQueryCreation() {
        Query query = new Query("Initial Query");

        assertNotNull(query);
        assertEquals("Initial Query", query.query());
    }

    @Test
    void testQueryWithQueryMethod() {
        Query query = new Query("Initial Query");
        Query updatedQuery = query.withQuery("Updated Query");

        assertNotNull(updatedQuery);
        assertEquals("Updated Query", updatedQuery.query());
        assertNotSame(query, updatedQuery);
    }
}
