import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import MovieDetail from "./MovieDetail.tsx";
import { IMovie } from "../types/Movie.ts";

interface WatchlistProps {
    user: string | undefined;
}

export default function Watchlist({ user }: WatchlistProps) {
    const navigate = useNavigate();
    const [movies, setMovies] = useState<IMovie[]>([]);
    const [selectedMovie, setSelectedMovie] = useState<IMovie | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function fetchWatchlist() {
            console.log("Watchlist: Starting to fetch watchlist for user:", user);
            if (!user || user === "Unauthorized") {
                console.log("Watchlist: User is not logged in. Redirecting to home.");
                navigate("/");
                return;
            }
            try {
                console.log("Watchlist: Fetching user details for watchlist.");
                const userRes = await axios.get(`/api/users/active/${user}`);
                console.log("Watchlist: User details received:", userRes.data);
                const favorites: string[] = userRes.data.favorites;
                if (!favorites || favorites.length === 0) {
                    console.log("Watchlist: No favorites found for user.");
                    setMovies([]);
                    setLoading(false);
                    return;
                }
                console.log("Watchlist: Favorites found:", favorites);
                const moviePromises = favorites.map((slug) => {
                    console.log("Watchlist: Fetching movie details for slug:", slug);
                    return axios.get(`/api/movies/${slug}`);
                });
                const responses = await Promise.all(moviePromises);
                const moviesData = responses.map((res) => res.data);
                console.log("Watchlist: Movies fetched:", moviesData);
                setMovies(moviesData);
                setLoading(false);
            } catch (err) {
                console.error("Watchlist: Error fetching watchlist:", err);
                setError("Failed to load watchlist.");
                setLoading(false);
            }
        }
        fetchWatchlist();
    }, [user, navigate]);

    if (loading) return <p>Loading watchlist...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div className="watchlist-container">
            <h2>Your Watchlist</h2>
            {movies.length === 0 ? (
                <p>Your watchlist is empty.</p>
            ) : selectedMovie ? (
                <MovieDetail
                    user={user}
                    movie={selectedMovie}
                    onBack={() => {
                        console.log("Watchlist: Returning to watchlist view from movie detail.");
                        setSelectedMovie(null);
                    }}
                />
            ) : (
                <div className="movies-list">
                    {movies.map((movie) => (
                        <div
                            key={movie.netzkinoId}
                            onClick={() => {
                                console.log("Watchlist: Movie clicked:", movie.title);
                                setSelectedMovie(movie);
                            }}
                            onKeyUp={() => {
                                console.log("Watchlist: Movie keyup triggered:", movie.title);
                                setSelectedMovie(movie);
                            }}
                            role="button"
                            tabIndex={0}
                            className="movie-item"
                            style={{
                                cursor: "pointer",
                                border: "1px solid #ccc",
                                borderRadius: "8px",
                                padding: "8px",
                                margin: "8px 0",
                            }}
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
