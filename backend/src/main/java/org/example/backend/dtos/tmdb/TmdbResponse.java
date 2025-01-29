package org.example.backend.dtos.tmdb;

import java.util.List;

public record TmdbResponse(
        List<TmdbMovieResult> movie_results,
        List<Object> person_results,
        List<Object> tv_results,
        List<Object> tv_episode_results,
        List<Object> tv_season_results
) {}