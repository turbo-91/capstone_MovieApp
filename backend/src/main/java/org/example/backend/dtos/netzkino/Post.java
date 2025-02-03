package org.example.backend.dtos.netzkino;

import java.util.List;
import java.time.OffsetDateTime;

public record Post(
        int id,
        String slug,
        String title,
        String content,
        OffsetDateTime date,
        OffsetDateTime modified,
        Author author,
        List<Integer> categories,
        String thumbnail,
        CustomFields custom_fields,
        List<String> properties,
        int _id,
        boolean _fullyLoaded,
        int sort_id,
        Match match
) {}

