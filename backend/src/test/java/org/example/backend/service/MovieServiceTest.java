package org.example.backend.service;

import org.example.backend.dtos.netzkino.*;
import org.example.backend.dtos.tmdb.TmdbMovieResult;
import org.example.backend.dtos.tmdb.TmdbResponse;
import org.example.backend.exceptions.DatabaseException;
import org.example.backend.model.Movie;
import org.example.backend.repo.MovieRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MovieServiceTest {

    private MovieRepo repo;
    private RestTemplate restTemplate;
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        repo = mock(MovieRepo.class);
        restTemplate = mock(RestTemplate.class);
        movieService = new MovieService(repo, restTemplate, "dummyTmdbApiKey", "dummyNetzkinoEnv");
    }

    @Test
    void getAllMovies_ShouldReturnListOfMovies_whenCalled() {
        // GIVEN
        Movie movie1 = new Movie("herr-der-ringe-1", "Herr der Ringe - Die Gefährten", 2001, "A journey begins.", "/image1.jpg");
        Movie movie2 = new Movie("herr-der-ringe-2", "Herr der Ringe - Die zwei Türme", 2002, "The adventure continues.", "/image2.jpg");

        List<Movie> movieList = List.of(movie1, movie2);
        when(repo.findAll()).thenReturn(movieList);

        // WHEN
        List<Movie> actual = movieService.getAllMovies();

        // THEN
        assertEquals(movieList, actual);
        verify(repo).findAll();
    }

    @Test
    void getMovieBySlug_ShouldReturnMovie_whenSlugExists() {
        // GIVEN
        String slug = "herr-der-ringe-1";
        Movie expectedMovie = new Movie(slug, "Herr der Ringe - Die Gefährten", 2001, "A journey begins.", "/image.jpg");
        when(repo.findBySlug(slug)).thenReturn(Optional.of(expectedMovie));

        // WHEN
        Movie actualMovie = movieService.getMovieBySlug(slug);

        // THEN
        assertEquals(expectedMovie, actualMovie);
        verify(repo).findBySlug(slug);
    }

    @Test
    void getMovieBySlug_ShouldThrowException_whenSlugDoesNotExist() {
        // GIVEN
        String nonExistentSlug = "non-existent-movie";
        when(repo.findBySlug(nonExistentSlug)).thenReturn(Optional.empty());

        // WHEN & THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> movieService.getMovieBySlug(nonExistentSlug));
        assertEquals("Movie with slug " + nonExistentSlug + " not found.", exception.getMessage());
        verify(repo).findBySlug(nonExistentSlug);
    }

    @Test
    void saveMovie_ShouldSaveAndReturnMovie_whenCalled() {
        // GIVEN
        Movie inputMovie = new Movie("new-movie", "New Movie", 2023, "A new story.", "/new-image.jpg");
        when(repo.save(inputMovie)).thenReturn(inputMovie);

        // WHEN
        Movie actual = movieService.saveMovie(inputMovie);

        // THEN
        assertEquals(inputMovie, actual);
        verify(repo).save(inputMovie);
    }

    @Test
    void updateMovie_ShouldUpdateAndReturnMovie_whenSlugExists() {
        // GIVEN
        String slug = "existing-movie";
        Movie updatedMovie = new Movie(slug, "Updated Movie", 2023, "Updated description.", "/updated.jpg");

        when(repo.existsBySlug(slug)).thenReturn(true);
        when(repo.save(updatedMovie)).thenReturn(updatedMovie);

        // WHEN
        Movie actual = movieService.updateMovie(updatedMovie);

        // THEN
        assertEquals(updatedMovie, actual);
        verify(repo).existsBySlug(slug);
        verify(repo).save(updatedMovie);
    }

    @Test
    void updateMovie_ShouldThrowException_whenSlugDoesNotExist() {
        // GIVEN
        String slug = "non-existent-movie";
        Movie movieToUpdate = new Movie(slug, "Non-existent Movie", 2023, "No data.", "/none.jpg");

        when(repo.existsBySlug(slug)).thenReturn(false);

        // WHEN & THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> movieService.updateMovie(movieToUpdate));
        assertEquals("Movie with slug " + slug + " does not exist.", exception.getMessage());
        verify(repo).existsBySlug(slug);
        verify(repo, never()).save(any());
    }

    @Test
    void deleteMovie_ShouldDeleteMovie_whenSlugExists() {
        // GIVEN
        String slug = "movie-to-delete";
        when(repo.existsBySlug(slug)).thenReturn(true);

        // WHEN
        movieService.deleteMovie(slug);

        // THEN
        verify(repo).existsBySlug(slug);
        verify(repo).deleteBySlug(slug);
    }

    @Test
    void deleteMovie_ShouldThrowException_whenSlugDoesNotExist() {
        // GIVEN
        String slug = "non-existent-movie";
        when(repo.existsBySlug(slug)).thenReturn(false);

        // WHEN & THEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> movieService.deleteMovie(slug));
        assertEquals("Movie with slug " + slug + " does not exist.", exception.getMessage());
        verify(repo).existsBySlug(slug);
        verify(repo, never()).deleteBySlug(any());
    }

    @Test
    void fetchAndStoreMovies_ShouldReturnMovies_whenApiReturnsResults() {
        // GIVEN
        String query = "Lord of the Rings";
        System.out.println("Starting test for fetchAndStoreMovies with query: " + query);

        CustomFields customFields = new CustomFields(
                null, // Adaptives_Streaming
                null, // Artikelbild
                null, // Duration
                null, // productionCountry
                null, // featured_img_all
                null, // featured_img_all_small
                null, // featured_img_seven
                null, // featured_img_slider
                null, // featured_img_logo
                null, // art_logo_img
                null, // hero_landscape_img
                null, // hero_portrait_img
                null, // primary_img
                null, // video_still_img
                null, // licenseStart
                null, // licenseEnd
                null, // activeCountries
                null, // skuAvod
                null, // skuSvod
                false, // drm
                null, // FSK
                null, // GEO_Availability_Exclusion
                null, // IMDb_Bewertung
                null, // IMDb_Link (This might be the issue)
                List.of("2001"), // Jahr - Correct Position
                null, // offlineAvailable
                null, // Regisseur
                null, // Stars
                null, // Streaming
                null, // TV_Movie_Cover
                null, // TV_Movie_Genre
                null, // Youtube_Deliverry_Active
                null, // Youtube_Delivery_Id
                null, // Youtube_Delivery_Preview_Only
                null, // Youtube_Delivery_Preview_Start
                null, // Youtube_Delivery_Preview_End
                null, // Featured_Video_Slider
                null, // featured_img_seven_small
                null // offlineAvailable
        );

        Post post = new Post(
                1, "lotr", "Lord of the Rings", "Epic fantasy.", null, null,
                null, null, null, customFields, null, 1, true, 1, null
        );

        NetzkinoResponse netzkinoResponse = new NetzkinoResponse(
                null, null, null, 1, 1, 1, null, List.of(post), null, 0, 0
        );

        // Mock the API response
        when(restTemplate.getForEntity(anyString(), eq(NetzkinoResponse.class)))
                .thenReturn(ResponseEntity.ok(netzkinoResponse));

        System.out.println("Mocked API response set up correctly.");

        // WHEN
        System.out.println("Calling fetchAndStoreMovies...");
        List<Movie> fetchedMovies = movieService.fetchAndStoreMovies(query);

        // THEN
        System.out.println("Number of movies returned: " + fetchedMovies.size());
        for (Movie movie : fetchedMovies) {
            System.out.println("Stored movie: " + movie.title() + ", Year: " + movie.year());
        }

        assertEquals(1, fetchedMovies.size(), "The number of stored movies should be 1");

        // Verify the attributes of the saved movie
        Movie savedMovie = fetchedMovies.get(0);
        assertEquals("lotr", savedMovie.slug(), "Expected slug to match");
        assertEquals("Lord of the Rings", savedMovie.title(), "Expected title to match");
        assertEquals("Epic fantasy.", savedMovie.overview(), "Expected overview to match");
        assertEquals(2001, savedMovie.year(), "Expected year to match");

        // Verify the batch save method was called with the correct list
        verify(repo).saveAll(fetchedMovies);
    }

    @Test
    void fetchAndStoreMovies_ShouldStoreMovieWithUnknownImdbId_WhenImdbIdIsMissing() {
        // GIVEN
        String query = "MissingImdbIdMovie";
        CustomFields customFields = new CustomFields(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, null, null, null, List.of("2005"), null, null, null, null, null, null, null, null, null, null, null, null, null);
        Post post = new Post(1, "slug", "Title", "Overview", null, null, new Author("AuthorName"), List.of(), null, customFields, List.of(), 1, true, 1, new Match("title", 0, query, query.length()));
        NetzkinoResponse response = new NetzkinoResponse(null, null, null, 1, 1, 1, null, List.of(post), null, 0, 0);

        // Mock the API call
        when(restTemplate.getForEntity(anyString(), eq(NetzkinoResponse.class))).thenReturn(ResponseEntity.ok(response));

        // WHEN
        List<Movie> fetchedMovies = movieService.fetchAndStoreMovies(query);

        // THEN
        assertEquals(1, fetchedMovies.size(), "Expected 1 movie to be fetched and saved");

        // Verify the attributes of the saved movie
        Movie savedMovie = fetchedMovies.get(0);
        assertEquals("slug", savedMovie.slug(), "Expected slug to match");
        assertEquals("Title", savedMovie.title(), "Expected title to match");
        assertEquals("Overview", savedMovie.overview(), "Expected overview to match");
        assertEquals("UNKNOWN", savedMovie.imgUrl(), "Expected IMDb ID to be UNKNOWN");

        // Verify the batch save method was called with the correct list
        verify(repo).saveAll(fetchedMovies);
    }

    @Test
    void fetchAndStoreMovies_ShouldStoreMovieWithDefaultYear_WhenYearIsInvalid() {
        // GIVEN
        String query = "InvalidYearMovie";

        // Create custom fields with an invalid year
        CustomFields customFields = new CustomFields(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, false, null, null, null, null, null, List.of("InvalidYear"),
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        // Create a post with invalid year
        Post post = new Post(
                1, "slug", "Title", "Overview", null, null,
                new Author("AuthorName"), List.of(), null, customFields, List.of(),
                1, true, 1, new Match("title", 0, query, query.length())
        );

        // Create a NetzkinoResponse with the post
        NetzkinoResponse response = new NetzkinoResponse(
                null, null, null, 1, 1, 1, null, List.of(post), null, 0, 0
        );

        // Mock the API call
        when(restTemplate.getForEntity(anyString(), eq(NetzkinoResponse.class))).thenReturn(ResponseEntity.ok(response));

        // WHEN
        List<Movie> fetchedMovies = movieService.fetchAndStoreMovies(query);

        // THEN
        assertEquals(1, fetchedMovies.size(), "Expected 1 movie to be fetched and saved");

        // Verify the attributes of the saved movie
        Movie savedMovie = fetchedMovies.get(0);
        assertEquals("slug", savedMovie.slug(), "Expected slug to match");
        assertEquals("Title", savedMovie.title(), "Expected title to match");
        assertEquals("Overview", savedMovie.overview(), "Expected overview to match");
        assertEquals(0, savedMovie.year(), "Expected default year to be 0");

        // Verify the batch save method was called with the correct list
        verify(repo).saveAll(fetchedMovies);
    }


    @Test
    void processNetzkinoMovie_ShouldReturnNull_WhenCustomFieldsAreNull() {
        // GIVEN
        Post post = new Post(1, "slug", "Title", "Overview", null, null, null, null, null, null, null, 1, true, 1, null);

        // WHEN
        Movie result = movieService.processNetzkinoMovie(post);

        // THEN
        assertNull(result, "Expected processNetzkinoMovie to return null when custom fields are null");
    }

    @Test
    void fetchMoviePosterFromTmdb_ShouldReturnNA_WhenTmdbInfoIsNull() {
        // GIVEN
        String imdbId = "tt1234567";
        String title = "Some Movie";
        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class)))
                .thenReturn(ResponseEntity.ok(null));

        // WHEN
        String result = movieService.fetchMoviePosterFromTmdb(imdbId, title);

        // THEN
        assertEquals("N/A", result, "Expected fetchMoviePosterFromTmdb to return 'N/A' when TMDB info is null");
    }

    @Test
    void fetchMoviePosterFromTmdb_ShouldReturnNA_WhenMovieResultsAreEmpty() {
        // GIVEN
        String imdbId = "tt1234567";
        String title = "Some Movie";

        // Create an empty TmdbResponse with all fields set appropriately
        TmdbResponse emptyResponse = new TmdbResponse(
                Collections.emptyList(), // movie_results
                Collections.emptyList(), // person_results
                Collections.emptyList(), // tv_results
                Collections.emptyList(), // tv_episode_results
                Collections.emptyList()  // tv_season_results
        );

        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        // WHEN
        String result = movieService.fetchMoviePosterFromTmdb(imdbId, title);

        // THEN
        assertEquals("N/A", result, "Expected fetchMoviePosterFromTmdb to return 'N/A' when movie results are empty");
    }


    @Test
    void fetchAndStoreMovies_ShouldReturnEmptyList_WhenNetzkinoResponseIsEmpty() {
        // GIVEN
        String query = "NoMovies";
        NetzkinoResponse emptyResponse = new NetzkinoResponse(null, null, null, 0, 0, 0, null, Collections.emptyList(), null, 0, 0);
        when(restTemplate.getForEntity(anyString(), eq(NetzkinoResponse.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        // WHEN
        List<Movie> result = movieService.fetchAndStoreMovies(query);

        // THEN
        assertTrue(result.isEmpty(), "Expected fetchAndStoreMovies to return an empty list when API returns no movies");
    }

    @Test
    void getAllMovies_ShouldThrowDatabaseException_WhenDatabaseFetchFails() {
        // GIVEN
        when(repo.findAll()).thenThrow(new DataAccessException("Database error") {});

        // WHEN & THEN
        DatabaseException exception = assertThrows(DatabaseException.class, () -> movieService.getAllMovies());
        assertEquals("Failed to fetch movies", exception.getMessage(), "Expected a specific exception message");
        assertNotNull(exception.getCause(), "Exception cause should not be null");
        assertEquals("Database error", exception.getCause().getMessage(), "Expected cause message to match the thrown exception");
    }


    @Test
    void extractImdbId_ShouldReturnNull_WhenImdbLinkIsInvalid() {
        // GIVEN
        CustomFields customFields = new CustomFields(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, null, null, null, List.of("invalid_imdb_link"), null, null, null, null, null, null, null, null, null, null, null, null, null);
        Post post = new Post(1, "slug", "Title", "Overview", null, null, null, List.of(), null, customFields, List.of(), 1, true, 1, null);

        // WHEN
        String imdbId = movieService.extractImdbId(post);

        // THEN
        assertNull(imdbId, "Expected extractImdbId to return null for an invalid IMDb link");
    }

//    @Test
//    void fetchMoviePosterFromTmdb_ShouldReturnNA_WhenPosterPathIsNull() {
//        // GIVEN
//        String imdbId = "tt1234567";
//        String title = "Movie Without Poster";
//
//        TmdbMovieResult movieResult = new TmdbMovieResult(
//                "id", 0, "original_language", "original_title",
//                "overview", "poster_path", "release_date", false,
//                "title", List.of(1), 0.0, "backdrop_path",
//                false, 0.0, 0
//        );
//        TmdbResponse tmdbResponse = new TmdbResponse(
//                List.of(movieResult),
//                Collections.emptyList(),
//                Collections.emptyList(),
//                Collections.emptyList(),
//                Collections.emptyList()
//        );
//
//        when(restTemplate.getForEntity(anyString(), eq(TmdbResponse.class)))
//                .thenReturn(ResponseEntity.ok(tmdbResponse));
//
//        // WHEN
//        String result = movieService.fetchMoviePosterFromTmdb(imdbId, title);
//
//        // THEN
//        assertEquals("N/A", result, "Expected fetchMoviePosterFromTmdb to return 'N/A' when poster_path is null");
//    }

    @Test
    void extractImdbId_ShouldReturnNull_WhenImdbLinksIsNull() {
        // GIVEN
        CustomFields customFields = new CustomFields(
                Collections.emptyList(), // field1
                Collections.emptyList(), // field2
                Collections.emptyList(), // field3
                "",                      // stringField
                Collections.emptyList(), // field5
                Collections.emptyList(), // field6
                Collections.emptyList(), // field7
                Collections.emptyList(), // field8
                Collections.emptyList(), // field9
                Collections.emptyList(), // field10
                Collections.emptyList(), // field11
                Collections.emptyList(), // field12
                Collections.emptyList(), // field13
                Collections.emptyList(), // field14
                OffsetDateTime.now(),    // offsetStart
                OffsetDateTime.now(),    // offsetEnd
                Collections.emptyList(), // field17
                "",                      // field18
                "",                      // field19
                false,                   // drm
                Collections.emptyList(), // field21
                Collections.emptyList(), // field22
                Collections.emptyList(), // field23
                Collections.emptyList(), // field24
                Collections.emptyList(), // field25
                Collections.emptyList(), // field26
                Collections.emptyList(), // field27
                Collections.emptyList(), // field28
                Collections.emptyList(), // field29
                Collections.emptyList(), // field30
                Collections.emptyList(), // field31
                Collections.emptyList(), // field32
                Collections.emptyList(), // field33
                Collections.emptyList(), // field34
                Collections.emptyList(), // field35
                Collections.emptyList(), // field36
                Collections.emptyList(), // field37
                Collections.emptyList(), // field38
                Collections.emptyList() // field39
        );

        Post post = new Post(1, "slug", "Title", "Overview", null, null, null, List.of(), null, customFields, List.of(), 1, true, 1, null);

        // WHEN
        String imdbId = movieService.extractImdbId(post);

        // THEN
        assertNull(imdbId, "Expected extractImdbId to return null when IMDb_Link is null");
    }


    @Test
    void extractYear_ShouldReturnZero_WhenYearIsInvalid() {
        // GIVEN
        CustomFields customFields = new CustomFields(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, true, null, null, null, null, null, null, List.of("InvalidYear"), null, null, null, null, null, null, null, null, null, null, null, null);
        Post post = new Post(1, "slug", "Title", "Overview", null, null, null, List.of(), null, customFields, List.of(), 1, true, 1, null);

        // WHEN
        int year = movieService.extractYear(post);

        // THEN
        assertEquals(0, year, "Expected extractYear to return 0 for an invalid year format");
    }

    @Test
    void processNetzkinoMovie_ShouldHandleUnknownImdbId() {
        // GIVEN
        CustomFields customFields = new CustomFields(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, true, null, null, null, null, null, null, List.of("InvalidYear"), null, null, null, null, null, null, null, null, null, null, null, null);
        Post post = new Post(1, "slug", "Title", "Overview", null, null, null, List.of(), null, customFields, List.of(), 1, true, 1, null);

        // WHEN
        Movie movie = movieService.processNetzkinoMovie(post);

        // THEN
        assertNotNull(movie, "Expected processNetzkinoMovie to create a Movie object");
        assertEquals("UNKNOWN", movie.imgUrl(), "Expected imgUrl to be 'UNKNOWN' when IMDb ID is missing");
    }

    @Test
    void processNetzkinoMovie_ShouldSetImgUrlToUnknown_WhenTmdbPosterIsNotFound() {
        // GIVEN
        CustomFields customFields = new CustomFields(
                Collections.emptyList(), // Example initialization
                Collections.emptyList(), Collections.emptyList(), null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), OffsetDateTime.now(), OffsetDateTime.now(),
                Collections.emptyList(), null, null, false,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList()
        );

        Post post = new Post(
                1, "slug", "Title", "Overview", null, null,
                null, Collections.emptyList(), null, customFields,
                Collections.emptyList(), 1, true, 1, null
        );

        // WHEN
        Movie movie = movieService.processNetzkinoMovie(post);

        // THEN
        assertNotNull(movie, "Expected processNetzkinoMovie to return a Movie object");
        assertEquals("UNKNOWN", movie.imgUrl(), "Expected imgUrl to be 'UNKNOWN' when TMDB poster is not found");
    }


}
