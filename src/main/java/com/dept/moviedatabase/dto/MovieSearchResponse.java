package com.dept.moviedatabase.dto;

import java.util.List;

public record MovieSearchResponse(
        int page,
        int totalPages,
        int totalResults,
        List<MovieDTO> results
) {}
