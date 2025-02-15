package org.example.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "users")
public record User(
        @Id String id,
        String githubId,
        String username,
        List<String> favorites
) {}
