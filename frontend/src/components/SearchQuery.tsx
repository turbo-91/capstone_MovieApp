import { useState, useEffect } from "react";
import MovieCard from "./MovieCard.tsx";

export default function SearchQuery() {
    const [query, setQuery] = useState("");
    const [movies, setMovies] = useState<any[]>([]);
    const [error, setError] = useState("");

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

    // ✅ Debounce the search input: trigger the search 500ms after the user stops typing.
    useEffect(() => {
        // If the query is empty, clear movies and do nothing.
        if (!query.trim()) {
            setMovies([]);
            return;
        }

        const debounceTimeout = setTimeout(() => {
            handleSearch();
        }, 700); // 700ms debounce delay

        // Clear the timeout if query changes before delay is over
        return () => clearTimeout(debounceTimeout);
    }, [query]);

    return (
        <div className="search-container">
            <h2>Search Movies</h2>
            <input
                type="text"
                value={query}
                onChange={handleInputChange}
                placeholder="Enter movie name"
            />
            {error && <p className="error">{error}</p>}
            <ul>
                {movies.map((movie: any) => (
                    <MovieCard key={movie.id} movie={movie}/>
                ))}
            </ul>
        </div>
    );
}
