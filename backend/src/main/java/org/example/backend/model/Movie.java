package org.example.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movies")
public record Movie(
        String slug,
        String title,
        String year,
        String overview,
        String imgUrl
) {

    public Movie(String slug, String title, String year, String overview, String imgUrl) {
        this.slug = slug;
        this.title = title;
        this.year = year;
        this.overview = overview;
        this.imgUrl = imgUrl;
    }

    public Movie withSlug(String slug) {
        return new Movie(this.slug, this.title, this.year, this.overview, this.imgUrl);
    }

}
