import { useState } from "react";
import axios from "axios";

type Movie = {
    slug: string;
    title: string;
    year: number;
    overview: string;
    imgUrl: string;
};

function App() {
    const [movies, setMovies] = useState<Movie[]>([]);
    const [query, setQuery] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const searchMovies = (query: string) => {
        console.log("query beginning searchMovies:", query)
        axios
            .get<Movie[]>(`http://localhost:8080/api/movies/search/${query}`) // Send query as a path variable
            .then((response) => {
                const allMovies = response.data; // List of movies from the backend
                console.log("Movies fetched successfully:", allMovies);
                setMovies(allMovies); // Update state with the fetched movies
            })
            .catch((error) => {
                console.error("Error fetching movies from the backend:", error);
            });
    };

    const handleSearch = () => {
        if (query.trim() !== "") {
            searchMovies(query);
        }
    };


    return (
        <div className="app">
            <h1>Movie Search</h1>

            {/* Search Input & Button */}
            <div>
                <input
                    type="text"
                    placeholder="Search for a movie..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)} // Update query state on input change
                />
                <button onClick={handleSearch}>Search</button>
            </div>

            {/* Loading Indicator */}
            {loading && <div>Loading movies...</div>}

            {/* Error Message */}
            {error && <div style={{ color: "red" }}>{error}</div>}

            {/* Movie List */}
            <div className="movie-list">
                {movies.length > 0 ? (
                    movies.map((movie) => (
                        <div key={movie.slug} className="movie-card">
                            <h2>{movie.title} ({movie.year})</h2>
                            <img src={movie.imgUrl} alt={movie.title} width="150" />
                            <p>{movie.overview}</p>
                        </div>
                    ))
                ) : (
                    !loading && <p>No movies found.</p>
                )}
            </div>
        </div>
    );
}
export default App;
