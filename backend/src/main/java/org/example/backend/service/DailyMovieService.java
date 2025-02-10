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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class DailyMovieService {

    private static final Logger logger = LoggerFactory.getLogger(DailyMovieService.class);
    private final MovieRepo movieRepository;
    private final RestTemplate restTemplate;
    private final QueryRepo queryRepository;
    private final String tmdbApiKey;
    private final String netzkinoEnv;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/find/";
    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/original";
    private static final String NETZKINO_URL = "https://api.netzkino.de.simplecache.net/capi-2.0a/search";

    private final List<String> predefinedNames = Arrays.asList(
            "Liam",
            "Noah",
            "Oliver",
            "James",
            "Elijah",
            "Mateo",
            "Theodore",
            "Henry",
            "Lucas",
            "William",
            "Benjamin",
            "Levi",
            "Sebastian",
            "Jack",
            "Ezra",
            "Michael",
            "Daniel",
            "Leo",
            "Owen",
            "Samuel",
            "Hudson",
            "Alexander",
            "Asher",
            "Luca",
            "Ethan",
            "John",
            "David",
            "Jackson",
            "Joseph",
            "Mason",
            "Luke",
            "Matthew",
            "Julian",
            "Dylan",
            "Elias",
            "Jacob",
            "Maverick",
            "Gabriel",
            "Logan",
            "Aiden",
            "Thomas",
            "Isaac",
            "Miles",
            "Grayson",
            "Santiago",
            "Anthony",
            "Wyatt",
            "Carter",
            "Jayden",
            "Ezekiel",
            "Caleb",
            "Cooper",
            "Josiah",
            "Charles",
            "Christopher",
            "Isaiah",
            "Nolan",
            "Cameron",
            "Nathan",
            "Joshua",
            "Kai",
            "Waylon",
            "Angel",
            "Lincoln",
            "Andrew",
            "Roman",
            "Adrian",
            "Aaron",
            "Wesley",
            "Ian",
            "Thiago",
            "Axel",
            "Brooks",
            "Bennett",
            "Weston",
            "Rowan",
            "Christian",
            "Theo",
            "Beau",
            "Eli",
            "Silas",
            "Jonathan",
            "Ryan",
            "Leonardo",
            "Walker",
            "Jaxon",
            "Micah",
            "Everett",
            "Robert",
            "Enzo",
            "Parker",
            "Jeremiah",
            "Jose",
            "Colton",
            "Luka",
            "Easton",
            "Landon",
            "Jordan",
            "Amir",
            "Gael",
            "Austin",
            "Adam",
            "Jameson",
            "August",
            "Xavier",
            "Myles",
            "Dominic",
            "Damian",
            "Nicholas",
            "Jace",
            "Carson",
            "Atlas",
            "Adriel",
            "Kayden",
            "Hunter",
            "River",
            "Greyson",
            "Emmett",
            "Harrison",
            "Vincent",
            "Milo",
            "Jasper",
            "Giovanni",
            "Jonah",
            "Zion",
            "Connor",
            "Sawyer",
            "Arthur",
            "Ryder",
            "Archer",
            "Lorenzo",
            "Declan",
            "Olivia",
            "Emma",
            "Charlotte",
            "Amelia",
            "Sophia",
            "Mia",
            "Isabella",
            "Ava",
            "Evelyn",
            "Luna",
            "Harper",
            "Sofia",
            "Camila",
            "Eleanor",
            "Elizabeth",
            "Violet",
            "Scarlett",
            "Emily",
            "Hazel",
            "Lily",
            "Gianna",
            "Aurora",
            "Penelope",
            "Aria",
            "Nora",
            "Chloe",
            "Ellie",
            "Mila",
            "Avery",
            "Layla",
            "Abigail",
            "Ella",
            "Isla",
            "Eliana",
            "Nova",
            "Madison",
            "Zoe",
            "Ivy",
            "Grace",
            "Lucy",
            "Willow",
            "Emilia",
            "Riley",
            "Naomi",
            "Victoria",
            "Stella",
            "Elena",
            "Hannah",
            "Valentina",
            "Maya",
            "Zoey",
            "Delilah",
            "Leah",
            "Lainey",
            "Lillian",
            "Paisley",
            "Genesis",
            "Madelyn",
            "Sadie",
            "Sophie",
            "Leilani",
            "Addison",
            "Natalie",
            "Josephine",
            "Alice",
            "Ruby",
            "Claire",
            "Kinsley",
            "Everly",
            "Emery",
            "Adeline",
            "Kennedy",
            "Maeve",
            "Audrey",
            "Autumn",
            "Athena",
            "Eden",
            "Iris",
            "Anna",
            "Eloise",
            "Jade",
            "Maria",
            "Caroline",
            "Brooklyn",
            "Quinn",
            "Aaliyah",
            "Vivian",
            "Liliana",
            "Gabriella",
            "Hailey",
            "Sarah",
            "Savannah",
            "Cora",
            "Madeline",
            "Natalia",
            "Ariana",
            "Lydia",
            "Lyla",
            "Clara",
            "Allison",
            "Aubrey",
            "Millie",
            "Melody",
            "Ayla",
            "Serenity",
            "Bella",
            "Skylar",
            "Josie",
            "Lucia",
            "Daisy",
            "Raelynn",
            "Eva",
            "Juniper",
            "Samantha",
            "Elliana",
            "Eliza",
            "Rylee",
            "Nevaeh",
            "Hadley",
            "Alaia",
            "Parker",
            "Julia",
            "Amara",
            "Rose",
            "Charlie",
            "Ashley",
            "Remi",
            "Georgia",
            "Adalynn",
            "Melanie",
            "Amira",
            "Margaret",
            "Piper"
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
        System.out.println("Fetching daily movies...");

        names = Optional.ofNullable(names).filter(list -> !list.isEmpty()).orElse(predefinedNames);
        LocalDate today = LocalDate.now();

        System.out.println("Today's date: " + today);
        System.out.println("Checking if movies are already stored for today...");

        List<Movie> existingMovies = movieRepository.findByDateFetchedContaining(today).orElse(List.of());

        if (!existingMovies.isEmpty()) {
            System.out.println("Found " + existingMovies.size() + " existing movies for today, returning them.");
            return existingMovies.stream().limit(5).collect(Collectors.toList());
        }

        String query = names.get(secureRandom.nextInt(names.size()));
        System.out.println("Selected query for fetching movies: " + query);

        if (queryRepository.findAll().stream().anyMatch(q -> q.query().contains(query))) {
            System.out.println("Query " + query + " has already been used. Fetching from database.");
            return movieRepository.findByQueriesContaining(query)
                    .orElse(List.of()).stream().limit(5).toList();
        }

        System.out.println("Query not used before, fetching new movies...");
        return fetchAndStoreMovies(query, today);
    }

    public List<Movie> fetchAndStoreMovies(String query, LocalDate today) {
        System.out.println("Fetching movies from external API using query: " + query);
        String netzkinoURL = NETZKINO_URL + "?q=" + query + "&d=" + netzkinoEnv;

        List<Movie> collectedMovies = new ArrayList<>();
        int maxRetries = 10; // Prevent infinite loops
        int retryCount = 0;

        while (collectedMovies.size() < 5 && retryCount < maxRetries) {
            retryCount++;

            try {
                ResponseEntity<NetzkinoResponse> response = restTemplate.getForEntity(netzkinoURL, NetzkinoResponse.class);

                if (response.getBody() == null || response.getBody().posts().isEmpty()) {
                    System.out.println("No movies found for query: " + query);
                } else {
                    System.out.println("Fetched " + response.getBody().posts().size() + " movies from external API");

                    String finalQuery = query;
                    List<Movie> newMovies = response.getBody().posts().stream()
                            .map(post -> {
                                try {
                                    if (post.custom_fields() == null) {
                                        System.out.println("Post has no custom fields, skipping...");
                                        return null;
                                    }

                                    String imdbLink = CustomFields.getOrDefault(post.custom_fields().IMDb_Link(), "");
                                    String imdbId = extractImdbId(imdbLink);

                                    if (imdbId.isEmpty()) {
                                        System.out.println("No valid IMDb ID found, skipping...");
                                        return null;
                                    }

                                    String imgImdb = fetchMoviePosterFromTmdb(imdbId);

                                    if ("N/A".equals(imgImdb)) {
                                        System.out.println("Image not found on TMDB, skipping movie: " + post.title());
                                        return null; // Drop movies that have no valid image
                                    }

                                    return formatMovieData(post, finalQuery, today, imgImdb);
                                } catch (Exception e) {
                                    System.out.println("Error formatting movie data: " + e.getMessage());
                                    e.printStackTrace();
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull) // Remove skipped movies
                            .collect(Collectors.toList());

                    collectedMovies.addAll(newMovies);

                    if (collectedMovies.size() >= 5) {
                        break; // Stop as soon as 5 movies are collected
                    }
                }

                // Pick a new random query and search again
                query = predefinedNames.get(secureRandom.nextInt(predefinedNames.size()));
                System.out.println("Retry " + retryCount + ": Trying new query -> " + query);
                netzkinoURL = NETZKINO_URL + "?q=" + query + "&d=" + netzkinoEnv;

            } catch (Exception e) {
                System.out.println("Error fetching movies: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // If retries exceeded, fail with an error
        if (collectedMovies.size() < 5) {
            throw new IllegalStateException("Failed to fetch 5 movies after " + maxRetries + " attempts.");
        }

        // Save and return only after at least 5 movies are found
        movieRepository.saveAll(collectedMovies);
        queryRepository.save(new Query(query));

        System.out.println("Stored " + collectedMovies.size() + " movies in database.");
        return collectedMovies;
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
        System.out.println("extractImdbId: Received IMDb link: " + imdbLink);
        return Optional.ofNullable(imdbLink)
                .filter(link -> link.contains("tt"))
                .map(link -> link.split("/"))
                .stream()
                .flatMap(Arrays::stream)
                .filter(part -> part.startsWith("tt"))
                .findFirst()
                .map(id -> {
                    System.out.println("extractImdbId: Extracted IMDb ID: " + id);
                    return id;
                })
                .orElse("");
    }


    public String fetchMoviePosterFromTmdb(String imdbId) {
        if (imdbId == null || imdbId.isEmpty()) {
            System.out.println("fetchMoviePosterFromTmdb: IMDb ID is null or empty, returning N/A");
            return "N/A";
        }

        String tmdbURL = TMDB_BASE_URL + imdbId + "?api_key=" + tmdbApiKey + "&language=de&external_source=imdb_id";
        System.out.println("fetchMoviePosterFromTmdb: Fetching TMDB poster using URL: " + tmdbURL);

        try {
            ResponseEntity<TmdbResponse> response = restTemplate.getForEntity(tmdbURL, TmdbResponse.class);

            if (response == null) {
                System.out.println("fetchMoviePosterFromTmdb: Response is null.");
                return "N/A";
            }
            if (response.getBody() == null) {
                System.out.println("fetchMoviePosterFromTmdb: Response body is null.");
                return "N/A";
            }
            if (response.getBody().movie_results() == null || response.getBody().movie_results().isEmpty()) {
                System.out.println("fetchMoviePosterFromTmdb: No movie results found for IMDb ID: " + imdbId);
                return "N/A";
            }

            TmdbMovieResult movieResult = response.getBody().movie_results().get(0);
            if (movieResult.backdrop_path() == null || movieResult.backdrop_path().isEmpty()) {
                System.out.println("fetchMoviePosterFromTmdb: No backdrop image found for IMDb ID: " + imdbId);
                return "N/A";
            }

            String imageUrl = TMDB_IMAGE_URL + movieResult.backdrop_path();
            System.out.println("fetchMoviePosterFromTmdb: Retrieved image URL: " + imageUrl);
            return imageUrl;

        } catch (Exception e) {
            System.out.println("fetchMoviePosterFromTmdb: Error fetching TMDB poster for IMDb ID " + imdbId + ": " + e.getMessage());
            e.printStackTrace();
            return "N/A";
        }
    }

}
