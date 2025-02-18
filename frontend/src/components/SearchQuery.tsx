import { useState } from "react";

export default function SearchQuery() {
    const [query, setQuery] = useState("");
    const [movies, setMovies] = useState([]);
    const [error, setError] = useState("");

    // ✅ Handles input while allowing only lowercase letters
    const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = event.target.value;
        if (/^[a-z]*$/.test(value)) { // ✅ Ensures only lowercase letters
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
        }
    };

    return (
        <div className="search-container">
            <h2>Search Movies</h2>
    <input
    type="text"
    value={query}
    onChange={handleInputChange}
    placeholder="Enter movie name"
    />
    <button onClick={handleSearch} disabled={!query.trim()}>
    Search
    </button>

    {error && <p className="error">{error}</p>}

        <ul>
        {movies.map((movie: any) => (
                <li key={movie.id}>{movie.title}</li>
            ))}
        </ul>
        </div>
    );
    }
