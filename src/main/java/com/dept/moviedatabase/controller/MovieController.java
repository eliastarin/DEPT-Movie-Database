package com.dept.moviedatabase.controller;

import com.dept.moviedatabase.dto.MovieSearchResponse;
import com.dept.moviedatabase.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Movies", description = "Movie search endpoints from TMDB.org") // Swagger Doc
@Validated
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }
    @Operation(
            summary = "Search movies by title", // Swagger Doc
            description = "Searches TMDB for matching movies."
    )
    @GetMapping("/search")
    public ResponseEntity<MovieSearchResponse> search(
            @RequestParam("q") @NotBlank String q,
            @RequestParam(value = "page", defaultValue = "1")
            @Min(1) @Max(500) int page,
            // Sort results based on yyyy
            @RequestParam(value = "sort", defaultValue = "relevance") String sort
    ) {
        return ResponseEntity.ok(movieService.search(q, page, sort));
    }
}
