package org.example.backend.service;

import org.example.backend.dtos.netzkino.*;
import org.example.backend.dtos.tmdb.TmdbMovieResult;
import org.example.backend.dtos.tmdb.TmdbResponse;
import org.example.backend.exceptions.DatabaseException;
import org.example.backend.model.Movie;
import org.example.backend.model.Query;
import org.example.backend.repo.MovieRepo;
import org.example.backend.repo.QueryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DailyMovieServiceTest {

    private MovieRepo movieRepository;
    private QueryRepo queryRepository;
    private RestTemplate restTemplate;
    private DailyMovieService dailyMovieService;

    @BeforeEach
    void setUp() {
        movieRepository = mock(MovieRepo.class);
        queryRepository = mock(QueryRepo.class);
        restTemplate = mock(RestTemplate.class);
        dailyMovieService = new DailyMovieService(movieRepository, restTemplate, queryRepository, "dummyTmdbApiKey", "dummyNetzkinoEnv");
    }

    @Test
    void getMoviesOfTheDay_ShouldReturnExistingMovies_WhenMoviesExistForToday() {
        // GIVEN
        LocalDate today = LocalDate.now();
        Movie movie = new Movie("1", 101, "slug-movie-1", "Inception", "2010", "A mind-bending thriller", "Christopher Nolan", "Leonardo DiCaprio", "img1", "img2", "img3", List.of("Sci-Fi"), List.of(today));
        when(movieRepository.findByDateFetchedContaining(today)).thenReturn(Optional.of(List.of(movie)));

        // WHEN
        List<Movie> actualMovies = dailyMovieService.getMoviesOfTheDay(List.of("Inception"));

        // THEN
        assertEquals(1, actualMovies.size());
        assertEquals(movie, actualMovies.get(0));
        verify(movieRepository).findByDateFetchedContaining(today);
    }

    @Test
    void getMoviesOfTheDay_ShouldFetchNewMovies_WhenNoMoviesExistForToday() {
        // GIVEN
        LocalDate today = LocalDate.now();
        when(movieRepository.findByDateFetchedContaining(today)).thenReturn(Optional.empty());
        when(queryRepository.findAll()).thenReturn(List.of());

        // Mock Netzkino API response
        CustomFields customFields = new CustomFields(
                List.of("https://example.com/streaming-url"),
                List.of("https://example.com/featured-img.jpg"),
                List.of("152 min"),
                "USA",
                List.of("https://example.com/featured-img.jpg"),
                List.of("https://example.com/featured-img-small.jpg"),
                List.of("https://example.com/featured-img-seven.jpg"),
                List.of("https://example.com/featured-img-slider.jpg"),
                List.of("https://example.com/featured-img-logo.jpg"),
                List.of("https://example.com/art-logo.jpg"),
                List.of("https://example.com/hero-landscape.jpg"),
                List.of("https://example.com/hero-portrait.jpg"),
                List.of("https://example.com/primary-img.jpg"),
                List.of("https://example.com/video-still.jpg"),
                OffsetDateTime.now().minusDays(30),
                OffsetDateTime.now().plusDays(30),
                List.of("DE", "AT"),
                "sku_avod_123",
                "sku_svod_456",
                false,
                List.of("FSK 12"),
                List.of("DE"),
                List.of("9.0"),
                List.of("https://imdb.com/title/tt0468569"),
                List.of("2008"),
                List.of("Yes"),
                List.of("Christopher Nolan"),
                List.of("Christian Bale, Heath Ledger"),
                List.of("Streaming Service"),
                List.of("https://example.com/tv-movie-cover.jpg"),
                List.of("Action"),
                List.of("true"),
                List.of("youtube_delivery_id"),
                List.of("false"),
                List.of("0"),
                List.of("60"),
                List.of("https://example.com/featured-video.jpg"),
                List.of("https://example.com/featured-img-seven-small.jpg"),
                List.of("Yes")
        );

        Post mockPost = new Post(
                102,
                "slug-the-dark-knight",
                "The Dark Knight",
                "Batman fights Joker",
                OffsetDateTime.now().minusDays(10),
                OffsetDateTime.now(),
                new Author("Christopher Nolan"),
                List.of(1, 2, 3),
                "https://example.com/thumbnail.jpg",
                customFields,
                List.of("property1", "property2"),
                56789,
                true,
                1,
                new Match("title", 0, "The Dark Knight", 12)
        );

        NetzkinoResponse mockNetzkinoResponse = new NetzkinoResponse(
                List.of("The Dark Knight"),
                "The Dark Knight",
                "success",
                1,
                1,
                1,
                1,
                List.of(mockPost),
                "slug-the-dark-knight",
                102,
                1
        );

        ResponseEntity<NetzkinoResponse> mockedResponseEntity = ResponseEntity.ok(mockNetzkinoResponse);
        when(restTemplate.getForEntity(anyString(), eq(NetzkinoResponse.class)))
                .thenReturn(mockedResponseEntity);

        // Mock TMDB API response
        TmdbResponse mockTmdbResponse = new TmdbResponse(
                List.of(new TmdbMovieResult(
                        "/sample-backdrop.jpg",
                        1,
                        "The Dark Knight",
                        "The Dark Knight Original",
                        "Some overview",
                        "/sample-backdrop.jpg",
                        "movie",
                        false,
                        "en",
                        List.of(28, 80),
                        9.0,
                        "2008-07-18",
                        false,
                        8.5,
                        1000
                )),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        ResponseEntity<TmdbResponse> tmdbResponseEntity = ResponseEntity.ok(mockTmdbResponse);

        // WHEN
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class)))
                .thenReturn(tmdbResponseEntity);
        List<Movie> movies = dailyMovieService.getMoviesOfTheDay(List.of("Inception"));




        // THEN
        assertEquals(5, movies.size());
        Movie movie = movies.get(0);
        assertEquals("The Dark Knight", movie.title());
        assertEquals("2008", movie.year());
        assertEquals("Christopher Nolan", movie.regisseur());
        assertEquals("Christian Bale, Heath Ledger", movie.stars());
        assertEquals("https://example.com/featured-img.jpg", movie.imgNetzkino());
        assertEquals("https://example.com/featured-img-small.jpg", movie.imgNetzkinoSmall());
        assertEquals("https://image.tmdb.org/t/p/original/sample-backdrop.jpg", movie.imgImdb());

        verify(movieRepository).findByDateFetchedContaining(today);
        verify(queryRepository).findAll();
        verify(movieRepository).saveAll(anyList());
        verify(queryRepository).save(any(Query.class));
    }




    @Test
    void getMoviesOfTheDay_ShouldReturnEmptyList_WhenFetchingFails() {
        // GIVEN
        when(movieRepository.findByDateFetchedContaining(any())).thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        // Prepare the input and precheck for exceptions
        List<String> movieList = List.of("Some Movie");
        doThrow(new RuntimeException("Database error"))
                .when(movieRepository)
                .findByDateFetchedContaining(any());

// Test the method invocation
        RuntimeException exception = assertThrows(RuntimeException.class, () -> dailyMovieService.getMoviesOfTheDay(movieList));

// Validate the exception message
        assertEquals("Database error", exception.getMessage());

    }

    @Test
    void fetchAndStoreMovies_ShouldFailWhenNoMoviesAreEverFound() {
        // GIVEN
        LocalDate today = LocalDate.now();
        List<String> movieQuery = List.of("Nonexistent");

        // Define the empty response
        NetzkinoResponse emptyResponse = new NetzkinoResponse(
                List.of(), // Empty list of movies
                "",        // Empty query string
                "success", // Status
                0,         // Total results
                0,         // Start index
                0,         // End index
                0,         // Number of pages
                List.of(), // Empty posts list
                "",        // Empty slug
                0,         // Some ID
                0          // Some count
        );

        when(movieRepository.findByDateFetchedContaining(today)).thenReturn(Optional.empty());
        when(restTemplate.getForEntity(anyString(), eq(NetzkinoResponse.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        // WHEN & THEN
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> dailyMovieService.getMoviesOfTheDay(movieQuery));

        // Validate exception message
        assertEquals("Failed to fetch 5 movies after 10 attempts.", exception.getMessage());
    }



    @Test
    void extractImdbId_ShouldReturnValidImdbId_WhenValidImdbLinkProvided() {
        // GIVEN
        String imdbLink = "https://www.imdb.com/title/tt1234567/";

        // WHEN
        String imdbId = dailyMovieService.extractImdbId(imdbLink);

        // THEN
        assertEquals("tt1234567", imdbId);
    }

    @Test
    void extractImdbId_ShouldReturnEmptyString_WhenInvalidImdbLinkProvided() {
        // GIVEN
        String invalidImdbLink = "invalid-link";

        // WHEN
        String imdbId = dailyMovieService.extractImdbId(invalidImdbLink);

        // THEN
        assertEquals("", imdbId);
    }

    @Test
    void extractImdbId_ShouldReturnEmptyString_WhenLinkIsNull() {
        // WHEN
        String imdbId = dailyMovieService.extractImdbId(null);

        // THEN
        assertEquals("", imdbId);
    }

    @Test
    void fetchMoviePosterFromTmdb_ShouldReturnNA_WhenTmdbApiFails() {
        // GIVEN
        String imdbId = "tt1234567";
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class))).thenReturn(ResponseEntity.ok(null));

        // WHEN
        String result = dailyMovieService.fetchMoviePosterFromTmdb(imdbId);

        // THEN
        assertEquals("N/A", result);
    }

    @Test
    void fetchMoviePosterFromTmdb_ShouldReturnNA_WhenResponseBodyIsNull() {
        // GIVEN
        String imdbId = "tt1234567";
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class))).thenReturn(null);

        // WHEN
        String result = dailyMovieService.fetchMoviePosterFromTmdb(imdbId);

        // THEN
        assertEquals("N/A", result);
    }

    @Test
    void fetchMoviePosterFromTmdb_ShouldHandleNullResponseGracefully() {
        // GIVEN
        String imdbId = "tt1234567";
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class))).thenReturn(null);

        // WHEN
        String imageUrl = dailyMovieService.fetchMoviePosterFromTmdb(imdbId);

        // THEN
        assertEquals("N/A", imageUrl);
    }

    @Test
    void fetchMoviePosterFromTmdb_ShouldHandleEmptyMovieResults() {
        // GIVEN
        String imdbId = "tt1234567";
        TmdbResponse tmdbResponse = new TmdbResponse(List.of(), List.of(), List.of(), List.of(), List.of());
        ResponseEntity<TmdbResponse> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(tmdbResponse);
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class))).thenReturn(responseEntity);

        // WHEN
        String imageUrl = dailyMovieService.fetchMoviePosterFromTmdb(imdbId);

        // THEN
        assertEquals("N/A", imageUrl);
    }

    @Test
    void fetchMoviePosterFromTmdb_ShouldHandleNullBackdropPath() {
        // GIVEN
        String imdbId = "tt1234567";
        TmdbMovieResult movieResult = new TmdbMovieResult(null, 1, "Title", "OriginalTitle", "Overview", "/poster.jpg", "movie", false, "en", List.of(), 8.0, "2022-01-01", false, 7.5, 100);
        TmdbResponse tmdbResponse = new TmdbResponse(List.of(movieResult), List.of(), List.of(), List.of(), List.of());
        ResponseEntity<TmdbResponse> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(tmdbResponse);
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class))).thenReturn(responseEntity);

        // WHEN
        String imageUrl = dailyMovieService.fetchMoviePosterFromTmdb(imdbId);

        // THEN
        assertEquals("N/A", imageUrl);
    }

    @Test
    void getMoviesOfTheDay_ShouldReturnMovies_WhenQueryWasPreviouslyUsed() {
        // GIVEN
        String query = "Inception";
        Query existingQuery = new Query(query);
        Movie movie = new Movie("1", 100, "slug", "title", "2010", "overview", "Christopher Nolan", "Leonardo DiCaprio", "img1", "img2", "img3", List.of(query), List.of(LocalDate.now()));

        when(queryRepository.findAll()).thenReturn(List.of(existingQuery));
        when(movieRepository.findByQueriesContaining(query)).thenReturn(Optional.of(List.of(movie)));

        // WHEN
        List<Movie> movies = dailyMovieService.getMoviesOfTheDay(List.of(query));

        // THEN
        assertEquals(1, movies.size());
        assertEquals(movie, movies.get(0));
        verify(queryRepository).findAll();
        verify(movieRepository).findByQueriesContaining(query);
    }

}






