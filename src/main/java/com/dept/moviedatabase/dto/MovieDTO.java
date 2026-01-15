package com.dept.moviedatabase.dto;

// Movie structure
public record MovieDTO(
        long id,
        String title,
        String releaseDate,
        String overview,
        String posterUrl
) {}
