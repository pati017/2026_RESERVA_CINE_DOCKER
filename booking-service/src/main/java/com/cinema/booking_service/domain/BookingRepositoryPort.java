package com.cinema.booking_service.domain;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepositoryPort {
    Booking save(Booking booking);
    Optional<Booking> findById(UUID id);
}
