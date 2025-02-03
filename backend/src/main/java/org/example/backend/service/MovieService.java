package org.example.backend.service;

import org.example.backend.dtos.netzkino.Post;
import org.example.backend.dtos.tmdb.TmdbMovieResult;
import org.example.backend.exceptions.DatabaseException;
import org.example.backend.model.Movie;
import org.example.backend.dtos.netzkino.NetzkinoResponse;
import org.example.backend.dtos.tmdb.TmdbResponse;
import org.example.backend.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepo movieRepo;
    private final RestTemplate restTemplate;

    private final String tmdbApiKey;
    private final String netzkinoEnv;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/find/";
    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String NETZKINO_URL = "https://api.netzkino.de.simplecache.net/capi-2.0a/search";

    public MovieService(MovieRepo movieRepo, RestTemplate restTemplate, @Value("${TMDB_API_KEY}") String tmdbApiKey, @Value("${NETZKINO_ENV}") String netzkinoEnv ) {
        this.movieRepo = movieRepo;
        this.restTemplate = restTemplate;
        this.tmdbApiKey = tmdbApiKey;
        this.netzkinoEnv=netzkinoEnv;
    }

    // database interactions
public List<Movie> getAllMovies() {
    try {
        List<Movie> movies = movieRepo.findAll();
        System.out.println("Service: Fetched all movies: " + movies);
        return movies;
    } catch (DataAccessException e) {
        throw new DatabaseException("Failed to fetch movies", e);
    }
}

    public Movie getMovieBySlug(String slug) {
        return movieRepo.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Movie with slug " + slug + " not found."));
    }

    public Movie saveMovie(Movie movie) {
        return movieRepo.save(movie);
    }

    public Movie updateMovie(Movie movie) {
        if (movieRepo.existsBySlug(movie.slug())) {
            return movieRepo.save(movie);
        } else {
            throw new IllegalArgumentException("Movie with slug " + movie.slug() + " does not exist.");
        }
    }

    public void deleteMovie(String slug) {
        if (!movieRepo.existsBySlug(slug)) {
            throw new IllegalArgumentException("Movie with slug " + slug + " does not exist.");
        }
        movieRepo.deleteBySlug(slug);
    }

    // fetch on search functionality

    public List<Movie> fetchAndStoreMovies(String query) {
        System.out.println("Starting movie fetching process...");

        String netzkinoUrl = String.format("%s?q=%s&d=%s", NETZKINO_URL, query, netzkinoEnv);
        System.out.println("Generated Netzkino URL: " + netzkinoUrl);

        ResponseEntity<NetzkinoResponse> netzkinoResponse = restTemplate.getForEntity(netzkinoUrl, NetzkinoResponse.class);
        NetzkinoResponse response = netzkinoResponse.getBody();

        if (response == null || response.posts() == null || response.posts().isEmpty()) {
            System.out.println("No movies found from API 1.");
            return Collections.emptyList();
        }

        System.out.println("Fetched " + response.posts().size() + " movies from API 1.");
        List<Movie> fetchedMovies = new ArrayList<>();

        for (Post post : response.posts()) {
            Movie movie = processNetzkinoMovie(post); // see processing in helper function below
            if (movie != null) {
                movieRepo.save(movie);
                fetchedMovies.add(movie);
            }
        }

        System.out.println("Finished processing all movies.");
        return fetchedMovies;
    }

    // Helper methods for fetchAndStoreMovies

    Movie processNetzkinoMovie(Post post) {
        System.out.println("Processing movie: " + post.title());

        if (post.custom_fields() == null) {
            System.out.println("Skipping movie due to missing custom fields.");
            return null;
        }

        String slug = post.slug();
        String title = post.title();
        String overview = post.content();
        int year = extractYear(post); // see below
        String imdbId = extractImdbId(post); // see below

        if (imdbId == null) {
            System.out.println("IMDb ID missing for: " + title + ", but movie will still be stored.");
            imdbId = "UNKNOWN"; // Assign a placeholder
        }

// Prevent TMDB API call if IMDb ID is "UNKNOWN"
        if ("UNKNOWN".equals(imdbId)) {
            System.out.println("Skipping TMDB API call for: " + title + " because IMDb ID is UNKNOWN.");
            return new Movie(slug, title, year, overview, "UNKNOWN"); // Store without a poster
        }

        String imgUrl = fetchMoviePosterFromTmdb(imdbId, title); // see below
        return new Movie(slug, title, year, overview, imgUrl);
    }

    int extractYear(Post post) {
        try {
            return (post.custom_fields().Jahr() != null && !post.custom_fields().Jahr().isEmpty())
                    ? Integer.parseInt(post.custom_fields().Jahr().get(0))
                    : 0;
        } catch (NumberFormatException e) {
            System.out.println("Error parsing year for: " + post.title());
            return 0;
        }
    }

    String extractImdbId(Post post) {
        List<String> imdbLinks = post.custom_fields().IMDb_Link();
        if (imdbLinks == null || imdbLinks.isEmpty()) {
            return null;
        }

        String imdbLink = imdbLinks.get(0);
        return imdbLink.contains("/tt") ? imdbLink.substring(imdbLink.lastIndexOf("/tt") + 1) : null;
    }

    String fetchMoviePosterFromTmdb(String imdbId, String title) {
String tmdbUrl = UriComponentsBuilder
    .fromHttpUrl(TMDB_BASE_URL)
    .pathSegment(imdbId)
    .queryParam("api_key", tmdbApiKey)
    .queryParam("language", "en-US")
    .queryParam("external_source", "imdb_id")
    .toUriString();

        System.out.println("Fetching additional info from TMDB: " + tmdbUrl);

        ResponseEntity<TmdbResponse> tmdbResponse = restTemplate.getForEntity(tmdbUrl, TmdbResponse.class);
        TmdbResponse tmdbInfo = tmdbResponse.getBody();

        if (tmdbInfo == null || tmdbInfo.movie_results().isEmpty()) {
            System.out.println("No additional entry found in TMDB for movie: " + title);
            return "N/A";
        }

        TmdbMovieResult movieResult = tmdbInfo.movie_results().get(0);
        return (movieResult.poster_path() != null) ? "https://image.tmdb.org/t/p/w500" + movieResult.poster_path() : "N/A";
    }

}
