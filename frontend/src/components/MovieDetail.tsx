import { IMovie } from "../types/Movie.ts";
import axios from "axios";
import { useState, useEffect } from "react";

interface MovieDetailProps {
    user: string | undefined; // GitHub ID of the user
    movie: IMovie;
    onBack: () => void; // Function to go back
}

export default function MovieDetail(props: Readonly<MovieDetailProps>) {
    const { movie, user, onBack } = props;
    const [message, setMessage] = useState("");
    const [isInWatchlist, setIsInWatchlist] = useState<boolean | null>(null); // ✅ Track watchlist status

    // ✅ Ensure we have a valid user before making API calls
    useEffect(() => {
        if (!user) {
            console.warn("User is undefined, skipping watchlist check.");
            return;
        }

        const fetchWatchlistStatus = async () => {
            console.log(`Fetching watchlist status for movie: ${movie.slug} and user: ${user}`);
            try {
                const response = await axios.get(`/api/users/watchlist/${user}/${movie.slug}`);
                console.log("Watchlist status response:", response.data);
                setIsInWatchlist(response.data.inWatchlist);
            } catch (error) {
                console.error("Error checking watchlist status:", error);
                setIsInWatchlist(false);
            }
        };

        fetchWatchlistStatus();
    }, [movie.slug, user]);

    // ✅ Toggle function for adding/removing from the watchlist
    const toggleWatchlist = async () => {
        if (!user) {
            console.warn("Cannot update watchlist, user is undefined.");
            return;
        }

        if (isInWatchlist === null) {
            console.warn("Toggle attempted before watchlist status was determined.");
            return;
        }

        try {
            if (isInWatchlist) {
                console.log(`Removing movie from watchlist: ${movie.slug} for user: ${user}`);
                await axios.delete(`/api/users/watchlist/${user}/${movie.slug}`);
                setMessage("Movie removed from watchlist.");
            } else {
                console.log(`Adding movie to watchlist: ${movie.slug} for user: ${user}`);
                await axios.post(`/api/users/watchlist/${user}/${movie.slug}`);
                setMessage("Movie added to watchlist.");
            }

            setIsInWatchlist(!isInWatchlist);
        } catch (error: any) {
            console.error("Error updating watchlist:", error);
            setMessage("Failed to update watchlist.");
        }
    };

    return (
        <div>
            {/* ✅ Back Button */}
            <button onClick={onBack} style={{ marginBottom: "10px" }}>⬅ Back</button>

            <h2>
                {movie.title} ({movie.year})
            </h2>
            <img src={movie.imgImdb} alt={`${movie.title} poster`} style={{ maxWidth: "300px" }} />
            <p>{movie.overview}</p>

            {/* ✅ Toggle Watchlist Button */}
            <button onClick={toggleWatchlist} disabled={isInWatchlist === null}>
                {isInWatchlist ? "Remove from Watchlist" : "Add to Watchlist"}
            </button>

            {message && <p>{message}</p>}
        </div>
    );
}
