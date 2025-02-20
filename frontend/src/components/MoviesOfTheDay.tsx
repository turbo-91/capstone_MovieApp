import MovieDetail from "./MovieDetail.tsx";
import { IMovie } from "../types/Movie.ts";
import { useEffect, useState } from "react";
import useSWR from "swr";
import { fetcher } from "../utils/fetcher.ts";
import styled from "styled-components";

interface MoviesOfTheDayProps {
    user: string | undefined;
}

// Styled Components
const MoviesContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem; // Space between movies
  padding: 2rem;
  width: 80vw;
  max-width: 1400px;
  color: white;
  font-family: Helvetica, Arial, sans-serif;
`;

const MovieItem = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s ease-in-out;

 

  img, h2 {
    cursor: pointer; // 🔥 Makes both image & title clickable
  }

  img {
    width: 100%;
    max-width: 800px; 
    height: auto;
  }

  h2 {
    margin-top: 1rem;
  }
`;

export default function MoviesOfTheDay(props: Readonly<MoviesOfTheDayProps>) {
    const { user } = props;
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
        <MoviesContainer>
            <h1>Movies of the Day</h1>
            {selectedMovie ? (
                <MovieDetail user={user} movie={selectedMovie} onBack={() => setSelectedMovie(null)} />
            ) : (
                data.map((movie: IMovie) => (
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
                ))
            )}
        </MoviesContainer>
    );
}
