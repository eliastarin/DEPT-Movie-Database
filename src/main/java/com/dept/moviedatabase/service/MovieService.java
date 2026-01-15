package com.dept.moviedatabase.service;

import com.dept.moviedatabase.dto.MovieDTO;
import com.dept.moviedatabase.dto.MovieSearchResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.Comparator;
import java.util.List;

@Service
public class MovieService {

    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p/w500";
    private final RestClient tmdbRestClient;

    public MovieService(RestClient tmdbRestClient) {
        this.tmdbRestClient = tmdbRestClient;
    }

    // Main searching functionality
    @Cacheable(
            value = "movieSearch",
            key = "#query + ':' + #page"
    )
    public MovieSearchResponse search(String query, int page, String sort) {

        // Validate user input to avoid unnecessary TMDB calls
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter 'q' must not be blank.");
        }

        // Page number always starts at 1
        int safePage = Math.max(1, page);

        // Normalize sorting input and apply a default when missing
        String safeSort = (sort == null || sort.isBlank())
                ? "relevance"
                : sort.trim().toLowerCase();

        try {
            // Call the TMDB /search/movie endpoint using the RestClient
            TmdbSearchMovieResponse tmdb = tmdbRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/movie")
                            .queryParam("query", query)
                            .queryParam("page", safePage)
                            .build())
                    .retrieve()
                    .body(TmdbSearchMovieResponse.class);

            // If TMDB returns a NULL response body
            if (tmdb == null) {
                return new MovieSearchResponse(1, 0, 0, List.of());
            }

            // Map the TMDB movie results to the internal MovieDTO objects
            List<MovieDTO> mapped = tmdb.results().stream()
                    .map(r -> new MovieDTO(
                            r.id(),
                            r.title(),
                            r.release_date(),
                            r.overview(),
                            r.poster_path() == null
                                    ? null
                                    : IMAGE_BASE + r.poster_path()
                    ))
                    .toList();

            // Apply an optional sorting logic based on the release date of the movie
            if (safeSort.startsWith("release_date")) {
                // Default: oldest movies first, null release dates last
                Comparator<MovieDTO> comparator = Comparator.comparing(
                        MovieDTO::releaseDate,
                        Comparator.nullsLast(String::compareTo)
                );
                // If requested, reverse order to show newest movies first
                if (safeSort.endsWith("_desc")) {
                    comparator = comparator.reversed();
                }
                mapped = mapped.stream()
                        .sorted(comparator)
                        .toList();
            }

            // Build and return the final API response
            return new MovieSearchResponse(
                    tmdb.page(),
                    tmdb.total_pages(),
                    tmdb.total_results(),
                    mapped
            );

        } catch (RestClientResponseException ex) {
            // Convert TMDB HTTP errors
            throw new RuntimeException(
                    "TMDB request failed: " + ex.getStatusCode().value(),
                    ex
            );
        }
    }

    // Internal DTOs to match to the TMDB JSON types
    record TmdbSearchMovieResponse(
            int page,
            int total_pages,
            int total_results,
            List<TmdbMovie> results
    ) {}

    record TmdbMovie(
            long id,
            String title,
            String release_date,
            String overview,
            String poster_path
    ) {}
}
