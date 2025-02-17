import {IMovie} from "../types/Movie.ts";

export interface SliderCardProps {
    movie: IMovie;
}

export default function SliderCard(props: Readonly<SliderCardProps>) {
    const { movie } = props;

    return (
        <>
            <h2>{movie.title}</h2>
            <p>{movie.year}</p>
            <h2>{movie.regisseur}</h2>
            <p>{movie.stars}</p>
            <img src={movie.imgImdb} alt={movie.title} width="200" />
        </>
    );
}
