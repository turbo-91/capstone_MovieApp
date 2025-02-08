package org.example.backend.service;

import org.example.backend.dtos.netzkino.CustomFields;
import org.example.backend.dtos.netzkino.NetzkinoResponse;
import org.example.backend.dtos.netzkino.Post;
import org.example.backend.dtos.tmdb.TmdbResponse;
import org.example.backend.model.Movie;
import org.example.backend.model.Query;
import org.example.backend.repo.MovieRepo;
import org.example.backend.repo.QueryRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.Random;

@Service
public class DailyMovieService {

    private final MovieRepo movieRepository;
    private final RestTemplate restTemplate;
    private QueryRepo queryRepository;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Value("${netzkino.env}")
    private String netzkinoEnv;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/find/";
    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String NETZKINO_URL = "https://api.netzkino.de.simplecache.net/capi-2.0a/search";

    private final List<String> predefinedNames = Arrays.asList(
            "Liam", "Noah", "Oliver", "James", "Elijah",
            "Sophia", "Isabella", "Ava", "Mia", "Charlotte"
    );


    public DailyMovieService(MovieRepo movieRepository, RestTemplate restTemplate, QueryRepo queryRepository, String tmdbApiKey, String netzkinoEnv) {
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
        this.queryRepository = queryRepository;
        this.tmdbApiKey = tmdbApiKey;
        this.netzkinoEnv = netzkinoEnv;
    }


    public List<Movie> getMoviesOfTheDay(List<String> names) {
        if (names == null || names.isEmpty()) {
            names = predefinedNames; // ✅ Use predefined list if parameter is empty
        }

        LocalDate today = LocalDate.now();
        List<Movie> existingMovies = movieRepository.findByDateFetchedContaining(today).orElse(List.of());

        if (!existingMovies.isEmpty()) {
            return existingMovies.stream().limit(5).collect(Collectors.toList());
        }

        String query = names.get(new Random().nextInt(names.size()));
        List<Query> usedQueries = queryRepository.findAll();



        if (usedQueries.stream().anyMatch(q -> q.query().contains(query))) {
            return movieRepository.findByQueriesContaining(query)
                    .map(movies -> movies.stream().limit(5).toList())
                    .orElse(List.of());
        }


        return fetchAndStoreMovies(query, today);
    }

    public List<Movie> fetchAndStoreMovies(String query, LocalDate today) {
        String netzkinoURL = "https://api.netzkino.de.simplecache.net/capi-2.0a/search?q=" + query + "&d=" + netzkinoEnv;

        ResponseEntity<NetzkinoResponse> response = restTemplate.getForEntity(netzkinoURL, NetzkinoResponse.class);
        if (response.getBody() == null || response.getBody().posts().isEmpty()) {
            throw new RuntimeException("Invalid or empty API response");
        }

        List<Movie> movies = response.getBody().posts().stream()
                .map(post -> {
                    String imdbLink = CustomFields.getOrDefault(post.custom_fields().IMDb_Link(), "");
                    String imdbId = extractImdbId(imdbLink);
                    String imgImdb = imdbId.isEmpty() ? "N/A" : fetchMoviePosterFromTmdb(imdbId);

                    return formatMovieData(post, query, today, imgImdb);
                })
                .collect(Collectors.toList());


        movieRepository.saveAll(movies);
        queryRepository.save(new Query(query));

        return movies;
    }


    public Movie formatMovieData(Post post, String query, LocalDate today, String imgImdb) {
        return new Movie(
                post.slug(),
                post.id(),
                post.slug(),
                post.title().trim(),
                CustomFields.getOrDefault(post.custom_fields().Jahr(), "0").trim(),
                post.content().trim(),
                CustomFields.getOrDefault(post.custom_fields().Regisseur(), "Unknown").trim(),
                CustomFields.getOrDefault(post.custom_fields().Stars(), "Unknown").trim(),  // ✅ FIX: Trim to remove spaces
                CustomFields.getOrDefault(post.custom_fields().featured_img_all(), "").trim(),
                CustomFields.getOrDefault(post.custom_fields().featured_img_all_small(), "").trim(),
                imgImdb.trim(),
                List.of(query),
                List.of(today)
        );
    }



    public String extractImdbId(String imdbLink) {
        if (imdbLink == null || !imdbLink.contains("tt")) {
            return "";
        }
        String[] parts = imdbLink.split("/");
        for (String part : parts) {
            if (part.startsWith("tt")) {
                return part;
            }
        }
        return "";
    }

    public String fetchMoviePosterFromTmdb(String imdbId) {
        String tmdbURL = "https://api.themoviedb.org/3/find/" + imdbId + "?api_key=" + tmdbApiKey + "&language=de&external_source=imdb_id";

        try {
            ResponseEntity<TmdbResponse> response = restTemplate.getForEntity(tmdbURL, TmdbResponse.class);
            if (response.getBody() != null && response.getBody().movie_results() != null && !response.getBody().movie_results().isEmpty()) {
                return "https://image.tmdb.org/t/p/w500" + response.getBody().movie_results().get(0).backdrop_path();
            }
        } catch (Exception e) {
            System.err.println("Error fetching TMDB poster: " + e.getMessage());
        }
        return "N/A";
    }

}
