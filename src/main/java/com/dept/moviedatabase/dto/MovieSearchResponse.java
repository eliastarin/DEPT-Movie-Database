package com.dept.moviedatabase.dto;

import java.util.List;

// Movie search response structure
public record MovieSearchResponse(
        int page,
        int totalPages,
        int totalResults,
        List<MovieDTO> results
) {}
