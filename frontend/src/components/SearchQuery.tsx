import { useState, useEffect } from "react";
import MovieDetail from "./MovieDetail.tsx";
import { IMovie } from "../types/Movie.ts";
import styled from "styled-components";

interface SearchQueryProps {
    user: string | undefined;
}

// Styled Components
const SearchContainer = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.5rem;
    padding: 2rem;
    width: 80vw;
    max-width: 1400px;
    color: white;
    font-family: Helvetica, Arial, sans-serif;
`;

const Input = styled.input`
    padding: 0.75rem;
    font-size: 1rem;
    width: 50%;
    max-width: 500px;
    border: none;
    border-radius: 4px;
    outline: none;
    text-align: center;
`;

const MoviesGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(3, 1fr); // 🔥 Always 3 movies per row
    gap: 2rem;
    justify-content: center;
    width: 100%;
    max-width: 1400px;

    @media (max-width: 1300px) {
        grid-template-columns: repeat(2, 1fr); // 2 movies per row on medium screens
    }

    @media (max-width: 900px) {
        grid-template-columns: repeat(1, 1fr); // 1 movie per row on smaller screens
    }
`;

const MovieItem = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    cursor: pointer;
    transition: transform 0.2s ease-in-out;

    

    img {
        width: 400px; // 🔥 Fixed width of 400px
        height: auto;
        cursor: pointer;
    }

    h2 {
        margin-top: 1rem;
        cursor: pointer;
    }
`;

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
        <SearchContainer>
            <h2>Search Movies</h2>
            <Input
                type="text"
                value={query}
                onChange={handleInputChange}
                placeholder="Enter movie name"
            />

            {error && <p className="error">{error}</p>}

            {movies.length === 0 ? (
                <p>No movies found.</p>
            ) : selectedMovie ? (
                <MovieDetail user={user} movie={selectedMovie} onBack={() => setSelectedMovie(null)} />
            ) : (
                <MoviesGrid>
                    {movies.map((movie) => (
                        <MovieItem
                            key={movie.netzkinoId}
                            onClick={() => setSelectedMovie(movie)}
                            onKeyUp={() => setSelectedMovie(movie)}
                            role="button"
                            tabIndex={0}
                        >
                            <img src={movie.imgImdb} alt={movie.title} />
                            <h2>{movie.title} ({movie.year})</h2>
                        </MovieItem>
                    ))}
                </MoviesGrid>
            )}
        </SearchContainer>
    );
}
