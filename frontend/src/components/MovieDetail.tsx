import {IMovie} from "../types/Movie.ts";
import axios from "axios";
import {useState} from "react";


interface MovieDetailProps {
    movie: IMovie;
    onBack: () => void;
}

    export default function MovieDetail(props: Readonly<MovieDetailProps>) {
        const { movie } = props;

        const [message, setMessage] = useState("");

        const addToWatchlist = async () => {
            console.log("addToWatchlist: Initiating API call to add movie with slug:", movie.slug);
            try {
                const response = await axios.post(`/api/users/watchlist/${movie.slug}`);
                console.log("addToWatchlist: Movie successfully added to watchlist.", response.data);
                setMessage("Movie added to watchlist.");
            } catch (error) {
                console.error("addToWatchlist: Error adding movie to watchlist:", error);
                setMessage("Failed to add movie to watchlist.");
            }
        };

        const removeFromWatchlist = async () => {
            console.log("removeFromWatchlist: Initiating API call to remove movie with slug:", movie.slug);
            try {
                const response = await axios.delete(`/api/users/watchlist/${movie.slug}`);
                console.log("removeFromWatchlist: Movie successfully removed from watchlist.", response.data);
                setMessage("Movie removed from watchlist.");
            } catch (error) {
                console.error("removeFromWatchlist: Error removing movie from watchlist:", error);
                setMessage("Failed to remove movie from watchlist.");
            }
        };

        return (
            <div>
                <h2>
                    {movie.title} ({movie.year})
                </h2>
                <img src={movie.imgImdb} alt={`${movie.title} poster`} style={{ maxWidth: "300px" }} />
                <p>{movie.overview}</p>
                <button onClick={addToWatchlist}>Add to Watchlist</button>
                <button onClick={removeFromWatchlist}>Remove from Watchlist</button>
                {message && <p>{message}</p>}
            </div>
        );
    }
