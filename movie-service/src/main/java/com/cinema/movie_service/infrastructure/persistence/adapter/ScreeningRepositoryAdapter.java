package com.cinema.movie_service.infrastructure.persistence.adapter;

import com.cinema.movie_service.domain.Screening;
import com.cinema.movie_service.domain.ScreeningRepositoryPort;
import com.cinema.movie_service.infrastructure.persistence.entity.ScreeningEntity;
import com.cinema.movie_service.infrastructure.persistence.repository.ScreeningJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScreeningRepositoryAdapter implements ScreeningRepositoryPort {

    private final ScreeningJpaRepository repository;

    @Override
    public Screening save(Screening screening) {
        ScreeningEntity entity = toEntity(screening);
        ScreeningEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Screening> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Screening> findByIdForUpdate(UUID id) {
        return repository.findByIdForUpdate(id).map(this::toDomain);
    }

    @Override
    public List<Screening> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ScreeningEntity toEntity(Screening domain) {
        return ScreeningEntity.builder()
                .id(domain.getId())
                .movieId(domain.getMovieId())
                .room(domain.getRoom())
                .showAt(domain.getShowAt())
                .totalSeats(domain.getTotalSeats())
                .availableSeats(domain.getAvailableSeats())
                .active(domain.isActive())
                .build();
    }

    private Screening toDomain(ScreeningEntity entity) {
        return Screening.builder()
                .id(entity.getId())
                .movieId(entity.getMovieId())
                .room(entity.getRoom())
                .showAt(entity.getShowAt())
                .totalSeats(entity.getTotalSeats())
                .availableSeats(entity.getAvailableSeats())
                .active(entity.isActive())
                .build();
    }
}
