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

    // Keep your existing method so current controller code still works
    public MovieSearchResponse search(String query, int page) {
        return search(query, page, "relevance");
    }

    // New method: supports sorting
    @Cacheable(
            value = "movieSearch",
            key = "#query + ':' + #page"
    )
    public MovieSearchResponse search(String query, int page, String sort) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter 'q' must not be blank.");
        }

        int safePage = Math.max(1, page);
        String safeSort = (sort == null || sort.isBlank()) ? "relevance" : sort.trim().toLowerCase();

        try {
            TmdbSearchMovieResponse tmdb = tmdbRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/movie")
                            .queryParam("query", query)
                            .queryParam("page", safePage)
                            .build())
                    .retrieve()
                    .body(TmdbSearchMovieResponse.class);

            if (tmdb == null) {
                return new MovieSearchResponse(1, 0, 0, List.of());
            }

            List<MovieDTO> mapped = tmdb.results().stream()
                    .map(r -> new MovieDTO(
                            r.id(),
                            r.title(),
                            r.release_date(),
                            r.overview(),
                            r.poster_path() == null ? null : IMAGE_BASE + r.poster_path()
                    ))
                    .toList();

            // Sorting logic
            if (safeSort.startsWith("release_date")) {
                Comparator<MovieDTO> comparator = Comparator.comparing(
                        MovieDTO::releaseDate,
                        Comparator.nullsLast(String::compareTo)
                );

                // release_date_desc = newest first
                if (safeSort.endsWith("_desc")) {
                    comparator = comparator.reversed();
                }

                mapped = mapped.stream()
                        .sorted(comparator)
                        .toList();
            }

            return new MovieSearchResponse(
                    tmdb.page(),
                    tmdb.total_pages(),
                    tmdb.total_results(),
                    mapped
            );

        } catch (RestClientResponseException ex) {
            throw new RuntimeException("TMDB request failed: " + ex.getStatusCode().value(), ex);
        }
    }

    // --- Internal DTOs to match TMDB JSON (only fields we need) ---

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
