package org.example.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "movies")
public record Movie(
        String slug,
        String title,
        int year,
        String overview,
        String imgUrl,
        List<String> searchQueries
) {

    public Movie withSlug(String slug) {
        return new Movie(slug, this.title, this.year, this.overview, this.imgUrl, this.searchQueries);
    }
}
