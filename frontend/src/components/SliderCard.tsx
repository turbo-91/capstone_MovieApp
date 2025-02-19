import { IMovie } from "../types/Movie.ts";
import { Link } from "react-router-dom";

export interface SliderCardProps {
    movie: IMovie;
}

export default function SliderCard(props: Readonly<SliderCardProps>) {
    const { movie } = props;

    return (
        <>
            <h2>{movie.title}</h2>
            <p>{movie.year}</p>
            <img src={movie.imgImdb} alt={movie.title} width="200" />
            <div>
                <Link to={`/movies/${movie.slug}`}>More</Link>
            </div>
        </>
    );
}
