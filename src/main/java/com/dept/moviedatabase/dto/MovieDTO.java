package com.dept.moviedatabase.dto;

public record MovieDTO(
        long id,
        String title,
        String releaseDate,
        String overview,
        String posterUrl
) {}
