package com.cinema.booking_service.infrastructure.persistence.adapter;

import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.domain.BookingRepositoryPort;
import com.cinema.booking_service.infrastructure.persistence.entity.BookingEntity;
import com.cinema.booking_service.infrastructure.persistence.repository.BookingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepositoryPort {

    private final BookingJpaRepository repository;

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = toEntity(booking);
        BookingEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private BookingEntity toEntity(Booking domain) {
        return BookingEntity.builder()
                .id(domain.getId())
                .screeningId(domain.getScreeningId())
                .movieTitle(domain.getMovieTitle())
                .customerId(domain.getCustomerId())
                .status(domain.getStatus())
                .totalAmount(domain.getTotalAmount())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Booking toDomain(BookingEntity entity) {
        return Booking.builder()
                .id(entity.getId())
                .screeningId(entity.getScreeningId())
                .movieTitle(entity.getMovieTitle())
                .customerId(entity.getCustomerId())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
