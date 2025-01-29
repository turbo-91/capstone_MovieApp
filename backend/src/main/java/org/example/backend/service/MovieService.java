package org.example.backend.service;

import org.example.backend.dtos.netzkino.Post;
import org.example.backend.dtos.tmdb.TmdbMovieResult;
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
        System.out.println("Generated Netzkino URL: " + netzkinoUrl);

        ResponseEntity<NetzkinoResponse> netzkinoResponse = restTemplate.getForEntity(netzkinoUrl, NetzkinoResponse.class);
        NetzkinoResponse response = netzkinoResponse.getBody();

        if (response == null || response.posts().isEmpty()) {
            System.out.println("No movies found from API 1.");
            return Collections.emptyList();
        }

        System.out.println("Fetched " + response.posts().size() + " movies from API 1.");
        List<Movie> fetchedMovies = new ArrayList<>();

        for (Post netzkinoMovie : response.posts()) {
            System.out.println("Processing movie: " + netzkinoMovie.title());

            String slug = netzkinoMovie.slug();
            String title = netzkinoMovie.title();
            String overview = netzkinoMovie.content();
            int year = 0;

            if (netzkinoMovie.custom_fields() != null
                    && netzkinoMovie.custom_fields().Jahr() != null
                    && !netzkinoMovie.custom_fields().Jahr().isEmpty()) {
                try {
                    year = Integer.parseInt(netzkinoMovie.custom_fields().Jahr().get(0));
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing year for: " + title);
                    continue;
                }
            }

            String imdbLink = (netzkinoMovie.custom_fields().IMDb_Link() != null && !netzkinoMovie.custom_fields().IMDb_Link().isEmpty())
                    ? netzkinoMovie.custom_fields().IMDb_Link().get(0)
                    : null;

            if (imdbLink == null || !imdbLink.contains("/tt")) {
                System.out.println("Skipping TMDB lookup, IMDb ID missing for: " + title);
                continue;
            }

            String imdbId = imdbLink.substring(imdbLink.lastIndexOf("/tt") + 1);
            System.out.println("Extracted IMDb ID: " + imdbId);

            String tmdbUrl = String.format("https://api.themoviedb.org/3/find/%s?api_key=%s&language=en-US&external_source=imdb_id",
                    imdbId, tmdbApiKey);

            System.out.println("Fetching additional info from TMDB: " + tmdbUrl);

            ResponseEntity<TmdbResponse> tmdbResponse = restTemplate.getForEntity(tmdbUrl, TmdbResponse.class);
            TmdbResponse tmdbInfo = tmdbResponse.getBody();

            if (tmdbInfo == null || tmdbInfo.movie_results().isEmpty()) {
                System.out.println("No additional entry found in TMDB for movie: " + title);
                continue;
            }

            TmdbMovieResult movieResult = tmdbInfo.movie_results().get(0);
            String imgUrl = (movieResult.poster_path() != null)
                    ? "https://image.tmdb.org/t/p/w500" + movieResult.poster_path()
                    : "N/A";

            System.out.println("Received additional info for " + title + ": " + imgUrl);

            Movie movieToSave = new Movie(slug, title, year, overview, imgUrl);
            movieRepo.save(movieToSave);
            fetchedMovies.add(movieToSave);

            System.out.println("Movie saved: " + movieToSave);
        }

        System.out.println("Finished processing all movies.");
        return fetchedMovies;
    }

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
