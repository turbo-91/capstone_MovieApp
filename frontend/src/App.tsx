import useSWR from "swr";
import {fetcher} from "./utils/fetcher.ts";


function App() {
    const { data, error } = useSWR("api/daily", fetcher, {
        shouldRetryOnError: false,
    });

    if (!data && !error) return <div>Loading...</div>;
    if (error) return <div>Error loading movies: {error.message}</div>;
    if (!data.length) return <p>No movies found.</p>;


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
