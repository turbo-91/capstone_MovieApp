package org.example.backend.dtos.tmdb;

import java.util.List;

public record TmdbMovieResult(
        String backdrop_path,
        int id,
        String title,
        String original_title,
        String overview,
        String poster_path,
        String media_type,
        boolean adult,
        String original_language,
        List<Integer> genre_ids,
        double popularity,
        String release_date,
        boolean video,
        double vote_average,
        int vote_count
) {}