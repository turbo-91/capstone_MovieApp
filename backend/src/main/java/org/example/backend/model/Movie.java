package org.example.backend.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "movies")
public record Movie(
        @Id String id,
        int netzkinoId,
        String slug,
        String title,
        String year,
        String overview,
        String regisseur,
        String stars,
        String imgNetzkino,
        String imgNetzkinoSmall,
        String imgImdb,
        List<String> queries,
        List<LocalDate> dateFetched
) {}