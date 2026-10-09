package com.cinema.movie_service.infrastructure.persistence.adapter;

import com.cinema.movie_service.domain.Movie;
import com.cinema.movie_service.domain.MovieRepositoryPort;
import com.cinema.movie_service.infrastructure.persistence.entity.MovieEntity;
import com.cinema.movie_service.infrastructure.persistence.repository.MovieJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MovieRepositoryAdapter implements MovieRepositoryPort {

    private final MovieJpaRepository repository;

    @Override
    public Movie save(Movie movie) {
        MovieEntity entity = toEntity(movie);
        MovieEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Movie> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Movie> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private MovieEntity toEntity(Movie domain) {
        return MovieEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .director(domain.getDirector())
                .genre(domain.getGenre())
                .durationMin(domain.getDurationMin())
                .rating(domain.getRating())
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Movie toDomain(MovieEntity entity) {
        return Movie.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .director(entity.getDirector())
                .genre(entity.getGenre())
                .durationMin(entity.getDurationMin())
                .rating(entity.getRating())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
