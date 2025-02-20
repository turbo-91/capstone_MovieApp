import { useState, useEffect } from "react";
import MovieDetail from "./MovieDetail.tsx";
import { IMovie } from "../types/Movie.ts";

interface SearchQueryProps {
    user: string | undefined;
}

export default function SearchQuery({ user }: SearchQueryProps) {
    const [query, setQuery] = useState("");
    const [movies, setMovies] = useState<IMovie[]>([]);
    const [error, setError] = useState("");
    const [selectedMovie, setSelectedMovie] = useState<IMovie | null>(null);

    // ✅ Handles input while allowing only lowercase letters
    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = event.target.value;
        if (/^[a-z]*$/.test(value)) { // Only allow lowercase letters
            setQuery(value);
            setError(""); // Clear error on valid input
        } else {
            setError("Only lowercase letters (a-z) are allowed.");
        }
    };

    // ✅ Handles search API call
    const handleSearch = async () => {
        if (!query.trim()) {
            setError("Search query cannot be empty.");
            setMovies([]);
            return;
        }
        try {
            const response = await fetch(`/api/movies/search?query=${query}`);
            if (!response.ok) throw new Error("Failed to fetch movies.");
            const data = await response.json();
            setMovies(data);
            setError("");
        } catch (err) {
            setError("Error fetching movies. Please try again.");
            setMovies([]);
        }
    };

    // ✅ Debounce the search input: trigger the search 700ms after the user stops typing.
    useEffect(() => {
        if (!query.trim()) {
            setMovies([]);
            return;
        }

        const debounceTimeout = setTimeout(() => {
            handleSearch();
        }, 700); // 700ms debounce delay

        return () => clearTimeout(debounceTimeout);
    }, [query]);

    return (
        <div className="search-container">
            {/* ✅ Heading and Input Field are ALWAYS visible */}
            <h2>Search Movies</h2>
            <input
                type="text"
                value={query}
                onChange={handleInputChange}
                placeholder="Enter movie name"
            />

            {error && <p className="error">{error}</p>}

            {/* ✅ If no movies are found, display message but keep input visible */}
            {movies.length === 0 ? (
                <p>No movies found.</p>
            ) : selectedMovie ? (
                <MovieDetail user={user} movie={selectedMovie} onBack={() => setSelectedMovie(null)} />
            ) : (
                <div className="movies-list">
                    {movies.map((movie) => (
                        <div
                            key={movie.netzkinoId}
                            onClick={() => setSelectedMovie(movie)}
                            onKeyUp={() => setSelectedMovie(movie)}
                            role="button"
                            tabIndex={0}
                            className="movie-item"
                        >
                            <h2>{movie.title}</h2>
                            <p>{movie.year}</p>
                            <h3>{movie.regisseur}</h3>
                            <p>{movie.stars}</p>
                            <img src={movie.imgImdb} alt={movie.title} width="200" />
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
