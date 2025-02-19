package org.example.backend.validation;

import org.example.backend.exceptions.InvalidSearchQueryException;

import java.util.regex.Pattern;

public class SearchQueryValidator {

    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("^[a-z]+$");

    public static void validate(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidSearchQueryException("Search query cannot be empty.");
        }
        if (!LOWERCASE_PATTERN.matcher(query).matches()) {
            throw new InvalidSearchQueryException("Search query must contain only lowercase letters (a-z).");
        }
    }
}

