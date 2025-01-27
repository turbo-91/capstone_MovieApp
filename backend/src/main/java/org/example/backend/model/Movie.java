package org.example.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movies")
public record Movie(
        int id,
        String slug,
        String title,
        String year,
        String overview,
        String imgUrl
) {

    public Movie(int id, String slug, String title, String year, String overview, String imgUrl) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.year = year;
        this.overview = overview;
        this.imgUrl = imgUrl;
    }

    public Movie withId(int id) {
        return new Movie(id, this.slug, this.title, this.year, this.overview, this.imgUrl);
    }

}
