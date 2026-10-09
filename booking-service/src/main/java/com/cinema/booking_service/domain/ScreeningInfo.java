package com.cinema.booking_service.domain;

import java.util.UUID;

/** Lo que booking-service necesita saber de una función de movie-service (modelo propio, sin DTOs de la API). */
public record ScreeningInfo(UUID id, UUID movieId, String movieTitle, int availableSeats, boolean active) {

    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }
}
