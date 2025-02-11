import useSWR from "swr";
import { fetcher } from "./utils/fetcher.ts";
import { IMovie } from "./types/Movie.ts";
import { useEffect, useState } from "react";
import MovieDetail from "./components/MovieDetail.tsx";

function App() {
    const [movies, setMovies] = useState<IMovie[]>([]);
    const { data, error } = useSWR("api/movies/daily", fetcher, {
        shouldRetryOnError: false,
    });

    const [selectedMovie, setSelectedMovie] = useState<IMovie | null>(null);

    useEffect(() => {
        if (data && movies.length === 0) {
            setMovies(data);
        }
    }, [data, movies]);

    if (!data && !error) return <div>Loading...</div>;
    if (error) return <div>Error loading movies: {error.message}</div>;
    if (!data?.length) return <p>No movies found.</p>;

    return (
        <div className="app">
            {selectedMovie ? (
                <MovieDetail
                    movie={selectedMovie}
                    onBack={() => setSelectedMovie(null)}
                />
            ) : (
                data.map((movie: IMovie) => (
                    <div
                        key={movie.netzkinoId}
                        onClick={() => setSelectedMovie(movie)}
                        onKeyUp={() => setSelectedMovie(movie)}
                        role="button"
                        tabIndex={0}
                    >
                        <h2>{movie.title}</h2>
                        <p>{movie.year}</p>
                        <h2>{movie.regisseur}</h2>
                        <p>{movie.stars}</p>
                        <img src={movie.imgImdb} alt={movie.title} width="200" />
                    </div>
                ))
            )}
        </div>
    );
}

export default App;
