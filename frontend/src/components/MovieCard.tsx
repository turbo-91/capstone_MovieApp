import {IMovie} from "../types/Movie.ts";
import {Link} from "react-router-dom";

export interface MovieCardProps {
    movie: IMovie;
}

export default function MovieCard(props: Readonly<MovieCardProps>) {
    const { movie } = props;
    return (
        <div
            className="movie-card"
        >
            <div style={{ position: 'relative', width: '120px', height: '180px' }}>
                <img
                    src={movie.imgImdb}
                    alt={`${movie.title} poster`}
                    width="200"
                />
            </div>
            <div className="movie-details" style={{ padding: '16px', flex: 1 }}>
                <h3 style={{ margin: '0 0 8px' }}>
                    {movie.title}{' '}
                    <span style={{ color: '#555', fontWeight: 'normal' }}>
            ({movie.year})
          </span>
                </h3>
                <p style={{ margin: '0 0 8px' }}>{movie.overview}</p>
                <p style={{ margin: '4px 0' }}>
                    <strong>Director:</strong> {movie.regisseur}
                </p>
                <p style={{ margin: '4px 0' }}>
                    <strong>Stars:</strong> {movie.stars}
                </p>
            </div>
            <Link to={`api/movies/${movie.slug}`}>More</Link>
        </div>
    );
}
