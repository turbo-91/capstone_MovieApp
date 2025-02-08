package org.example.backend.service;

import org.example.backend.dtos.netzkino.CustomFields;
import org.example.backend.dtos.netzkino.NetzkinoResponse;
import org.example.backend.dtos.netzkino.Post;
import org.example.backend.dtos.tmdb.TmdbMovieResult;
import org.example.backend.dtos.tmdb.TmdbResponse;
import org.example.backend.model.Movie;
import org.example.backend.model.Query;
import org.example.backend.repo.MovieRepo;
import org.example.backend.repo.QueryRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DailyMovieService {

    private final MovieRepo movieRepository;
    private final RestTemplate restTemplate;
    private final QueryRepo queryRepository;
    private final String tmdbApiKey;
    private final String netzkinoEnv;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/find/";
    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w500";
    private static final String NETZKINO_URL = "https://api.netzkino.de.simplecache.net/capi-2.0a/search";

    private final List<String> predefinedNames = Arrays.asList(
            "Liam", "Noah", "Oliver", "James", "Elijah",
            "Sophia", "Isabella", "Ava", "Mia", "Charlotte"
    );

    private static final SecureRandom secureRandom = new SecureRandom();

    public DailyMovieService(MovieRepo movieRepository, RestTemplate restTemplate, QueryRepo queryRepository, @Value("${TMDB_API_KEY}") String tmdbApiKey, @Value("${NETZKINO_ENV}") String netzkinoEnv) {
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
        this.queryRepository = queryRepository;
        this.tmdbApiKey = tmdbApiKey;
        this.netzkinoEnv = netzkinoEnv;
    }

    public List<Movie> getMoviesOfTheDay(List<String> names) {
        names = Optional.ofNullable(names).filter(list -> !list.isEmpty()).orElse(predefinedNames);

        LocalDate today = LocalDate.now();
        List<Movie> existingMovies = movieRepository.findByDateFetchedContaining(today).orElse(List.of());

        if (!existingMovies.isEmpty()) {
            return existingMovies.stream().limit(5).collect(Collectors.toList());
        }

        String query = names.get(secureRandom.nextInt(names.size()));
        List<Query> usedQueries = queryRepository.findAll();

        if (usedQueries.stream().anyMatch(q -> q.query().contains(query))) {
            return movieRepository.findByQueriesContaining(query)
                    .orElse(List.of()).stream().limit(5).toList();
        }

        return fetchAndStoreMovies(query, today);
    }

    public List<Movie> fetchAndStoreMovies(String query, LocalDate today) {
        String netzkinoURL = NETZKINO_URL + "?q=" + query + "&d=" + netzkinoEnv;
        ResponseEntity<NetzkinoResponse> response = restTemplate.getForEntity(netzkinoURL, NetzkinoResponse.class);

        List<Movie> movies = response.getBody().posts().stream()
                .map(post -> {
                    if (post == null || post.custom_fields() == null) {
                        throw new NullPointerException("Post or Custom Fields is null");
                    }
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
        return Optional.ofNullable(imdbLink)
                .filter(link -> link.contains("tt"))
                .map(link -> link.split("/"))
                .stream()
                .flatMap(Arrays::stream)
                .filter(part -> part.startsWith("tt"))
                .findFirst()
                .orElse("");
    }

    public String fetchMoviePosterFromTmdb(String imdbId) {
        if (imdbId == null || imdbId.isEmpty()) {
            return "N/A";
        }
        String tmdbURL = TMDB_BASE_URL + imdbId + "?api_key=" + tmdbApiKey + "&language=de&external_source=imdb_id";
        try {
            ResponseEntity<TmdbResponse> response = restTemplate.getForEntity(tmdbURL, TmdbResponse.class);
            return Optional.ofNullable(response)
                    .map(ResponseEntity::getBody)
                    .map(TmdbResponse::movie_results)
                    .filter(results -> !results.isEmpty())
                    .map(results -> results.get(0).backdrop_path())
                    .filter(path -> !path.isEmpty())
                    .map(path -> TMDB_IMAGE_URL + path)
                    .orElse("N/A");
        } catch (Exception e) {
            System.err.println("Error fetching TMDB poster: " + e.getMessage());
        }
        return "N/A";
    }
}
