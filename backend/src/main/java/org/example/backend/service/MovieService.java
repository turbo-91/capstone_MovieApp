package org.example.backend.service;

import org.example.backend.model.Movie;
import org.example.backend.dtos.netzkino.NetzkinoResponse;
import org.example.backend.dtos.tmdb.TmdbResponse;
import org.example.backend.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepo movieRepo;
    private final RestTemplate restTemplate;

    @Value("${TMDB_API_KEY}")
    private String tmdbApiKey;

    @Value("${NETZKINO_ENV}")
    private String netzkinoEnv;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/find/";
    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String NETZKINO_URL = "https://api.netzkino.de.simplecache.net/capi-2.0a/search";

    public MovieService(MovieRepo movieRepo, RestTemplate restTemplate) {
        this.movieRepo = movieRepo;
        this.restTemplate = restTemplate;
    }

    public List<Movie> fetchAndStoreMovies(String query) {
        System.out.println("Starting movie fetching process...");

        String netzkinoUrl = String.format("%s?q=%s&d=%s", NETZKINO_URL, query, netzkinoEnv);

        // Step 1: Fetch movies from API 1
        ResponseEntity<NetzkinoResponse[]> netzkinoResponse = restTemplate.getForEntity(netzkinoUrl, NetzkinoResponse[].class);
        NetzkinoResponse[] netzkinoResponses = netzkinoResponse.getBody();

        if (netzkinoResponses == null) {
            System.out.println("No movies found from API 1.");
            return Collections.emptyList();
        }

        System.out.println("Fetched " + netzkinoResponses.length + " movies from API 1.");

        List<Movie> fetchedMovies = new ArrayList<>();

        // Step 2: Get each movie
        for (NetzkinoResponse netzkinoMovie : netzkinoResponses) {
            System.out.println("Processing movie: " + netzkinoMovie.title());

            // Extract necessary values
            String slug = netzkinoMovie.slug();
            String title = netzkinoMovie.title();
            int year = netzkinoMovie.year();
            String overview = netzkinoMovie.overview();
            String imdbLink = netzkinoMovie.imdbLink(); // will be used to retrieve image from tmdb API

            // Base TMDB URL and API key
            String imdbId = imdbLink.substring(imdbLink.lastIndexOf("/") + 1);
            System.out.println("extract imdb ID: " + imdbId);
            String tmdbBaseUrl = "https://api.themoviedb.org/3/find/";
            String apiKey = "78247849b9888da02ffb1655caa3a9b9"; // Replace with your actual API key
            String tmdbUrl = String.format("%s%s?api_key=%s&language=en-US&external_source=imdb_id",
                    tmdbBaseUrl, imdbId, apiKey);
            System.out.println("Full tmdb URL: " + tmdbUrl);

            // Fetch each movie from API 2
            System.out.println("Fetching additional info from tmdb: " + tmdbUrl);

            ResponseEntity<TmdbResponse> tmdbResponse = restTemplate.getForEntity(tmdbUrl, TmdbResponse.class);
            TmdbResponse tmdbInfo = tmdbResponse.getBody();

            if (tmdbInfo == null) {
                System.out.println("No additional details found for movie: " + title);
                continue;
            }

            // Extract specific value from API 2
            String imgUrl = tmdbInfo.movieUrl();
            System.out.println("Received additional info for " + title + ": " + imgUrl);


            // Step 4: Create and save the movie object
            Movie movieToSave = new Movie(slug, title, year, overview, imgUrl);
            movieRepo.save(movieToSave);
            fetchedMovies.add(movieToSave);

            System.out.println("Movie saved: " + movieToSave);
        }
        System.out.println("Finished processing all movies.");
        return fetchedMovies;
    }
    ;

    public List<Movie> getAllMovies() {
        try {
            List<Movie> movies = movieRepo.findAll();
            System.out.println("Service: Fetched all movies: " + movies);
            return movies;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch movies from the database.", e);
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

}
