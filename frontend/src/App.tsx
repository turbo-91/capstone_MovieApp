import useSWR from "swr";
import {fetcher} from "./utils/fetcher.ts";
import {IMovie} from "./types/Movie.ts";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import {useEffect, useState} from "react";

function App() {
    const [movies, setMovies] = useState([]);
    const { data, error } = useSWR("api/movies/daily", fetcher, {
        shouldRetryOnError: false,
    });

    useEffect(() => {
        if (data && !movies.length) {
            setMovies(data);
        }
    }, [data, movies]);

    if (!data && !error) return <div>Loading...</div>;
    if (error) return <div>Error loading movies: {error.message}</div>;
    if (!data.length) return <p>No movies found.</p>;


    return (
        <div className="app">
            {/*<div className="movie-list">*/}
            {/*        <Slider {...settings}>*/}
            {/*        {data.map((movie: IMovie) => (*/}
            {/*                <SliderCard key={movie.netzkinoId} movie={movie} />*/}
            {/*            ))}*/}
            {/*        </Slider>*/}
            {/*</div>*/}
                    {data.map((movie: IMovie) => (
                            <><h2>{movie.title}</h2>
                                <p>{movie.year}</p>
                                <h2>{movie.regisseur}</h2>
                                <p>{movie.stars}</p>
                                <img src={movie.imgImdb} alt={movie.title} width="200" /></>
                        ))}
        </div>
    );
}
export default App;
