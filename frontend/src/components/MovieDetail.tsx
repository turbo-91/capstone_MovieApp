import {IMovie} from "../types/Movie.ts";

interface MovieDetailProps {
    movie: IMovie;
    onBack: () => void;
}

export default function MovieDetail({ movie, onBack }: MovieDetailProps) {
    return (
        <div className="p-4 bg-gray-800 text-white rounded-lg">
            <button onClick={onBack} className="text-red-500">
                Back to Movies
            </button>
            <h2 className="text-2xl">{movie.title}</h2>
            <p>{movie.overview}</p>
            <p>{movie.regisseur}</p>
            <p>{movie.stars}</p>
            <img src={movie.imgImdb} alt={movie.title} className="mt-2 rounded" />
        </div>
    );
}
