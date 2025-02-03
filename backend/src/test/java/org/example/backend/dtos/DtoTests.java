package org.example.backend.dtos;

import org.example.backend.dtos.tmdb.TmdbMovieResult;
import org.example.backend.dtos.tmdb.TmdbResponse;
import org.example.backend.dtos.netzkino.*;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class DtoTests {

    @Test
    void testTmdbMovieResult() {
        TmdbMovieResult movie = new TmdbMovieResult(
                "/path/to/backdrop", 123, "Movie Title", "Original Title",
                "Overview of the movie", "/path/to/poster", "movie",
                false, "en", List.of(1, 2, 3), 8.9,
                "2023-01-01", false, 7.5, 200
        );

        assertThat(movie.id()).isEqualTo(123);
        assertThat(movie.title()).isEqualTo("Movie Title");
        assertThat(movie.vote_average()).isEqualTo(7.5);
    }

    @Test
    void testTmdbResponse() {
        TmdbResponse response = new TmdbResponse(List.of(), List.of(), List.of(), List.of(), List.of());
        assertThat(response.movie_results()).isEmpty();
    }

    @Test
    void testAuthor() {
        Author author = new Author("John Doe");
        assertThat(author.name()).isEqualTo("John Doe");
    }

    @Test
    void testCustomFields() {
        CustomFields customFields = new CustomFields(List.of("stream1"), List.of("image1"), List.of("120min"), "USA", List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), OffsetDateTime.now(), OffsetDateTime.now(), List.of("US"), "skuAvod", "skuSvod", true,
                List.of("FSK16"), List.of(), List.of("8.5"), List.of("link"), List.of("2022"), List.of("yes"), List.of("director"),
                List.of("star1"), List.of("streaming1"), List.of("cover1"), List.of("genre1"), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of());

        assertThat(customFields.productionCountry()).isEqualTo("USA");
        assertThat(customFields.drm()).isTrue();
    }

    @Test
    void testMatch() {
        Match match = new Match("title", 1, "query", 10);
        assertThat(match.field()).isEqualTo("title");
        assertThat(match.length()).isEqualTo(10);
    }

    @Test
    void testPost() {
        Author author = new Author("John Doe");
        Match match = new Match("title", 1, "query", 10);
        CustomFields customFields = new CustomFields(List.of(), List.of(), List.of(), "USA", List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), OffsetDateTime.now(), OffsetDateTime.now(), List.of(), "sku", "sku", true,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());


        Post post = new Post(1, "slug", "title", "content", OffsetDateTime.now(), OffsetDateTime.now(), author,
                List.of(1, 2, 3), "thumbnail", customFields, List.of(), 1, true, 100, match);

        assertThat(post.id()).isEqualTo(1);
        assertThat(post.title()).isEqualTo("title");
        assertThat(post._fullyLoaded()).isTrue();
    }

    @Test
    void testNetzkinoResponse() {
        NetzkinoResponse response = new NetzkinoResponse(List.of("query"), "search", "ok", 100, 10, 1, 10, List.of(), "slug", 1, 10);
        assertThat(response.searchTerm()).isEqualTo("search");
        assertThat(response.count_total()).isEqualTo(100);
    }
}
