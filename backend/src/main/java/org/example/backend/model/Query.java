package org.example.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "queries")
public record Query(
        String query
) {
    public Query withQuery(String query) {
        return new Query(query);
    }
}

