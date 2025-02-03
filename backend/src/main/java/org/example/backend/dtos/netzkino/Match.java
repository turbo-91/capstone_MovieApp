package org.example.backend.dtos.netzkino;

public record Match(
        String field,
        int index,
        String query,
        int length
) {}
