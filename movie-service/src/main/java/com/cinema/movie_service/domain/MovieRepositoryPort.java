package com.cinema.movie_service.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovieRepositoryPort {
    Movie save(Movie movie);
    Optional<Movie> findById(UUID id);
    List<Movie> findAll();
    void deleteById(UUID id);
}
