package org.example.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movies")
public record Movie(
        String slug,
        String title,
        int year,
        String overview,
        String imgUrl
) {

    public Movie withSlug(String slug) {
        return new Movie(slug, this.title, this.year, this.overview, this.imgUrl);
    }
}
