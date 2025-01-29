import { useEffect, useState } from "react";
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
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchMovies = async () => {
            try {
                setLoading(true);
                setError(null);

                // Send a GET request with query parameters
                const response = await axios.get("/api/movies/combine", {
                    params: {
                        query: "pete",
                        env: "devtest",
                        imdbId: "tt0036621",
                    },
                });

                // Update state with the fetched movie
                setMovies([response.data]); // Backend returns a single movie
            } catch (err) {
                console.error("Error fetching movie:", err);
                setError("Failed to fetch movie. Please try again later.");
            } finally {
                setLoading(false);
            }
        };

        fetchMovies();
    }, []); // Fetch once on mount

    if (loading) return <div>Loading movies...</div>;
    if (error) return <div>{error}</div>;

    return (
        <div className="movie-list">
            {movies.map((movie) => (
                <div key={movie.slug} className="movie">
                    <p><strong>{movie.title}</strong> ({movie.year})</p>
                    <img src={movie.imgUrl} alt={`${movie.title} poster`} />
                    <p>{movie.overview}</p>
                </div>
            ))}
        </div>
    );
}

export default App;
