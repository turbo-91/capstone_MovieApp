package org.example.backend.dtos.netzkino;


import java.util.List;

public record NetzkinoResponse(
        List<String> _qryArr,
        String searchTerm,
        String status,
        int count_total,
        int count,
        int page,
        Integer pages,
        List<Post> posts,
        String slug,
        int id,
        int post_count
) {}