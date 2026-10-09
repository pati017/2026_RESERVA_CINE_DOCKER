package com.cinema.booking_service.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/** Contrato JSON de GET /screenings/{id} en movie-service (solo los campos que interesan). */
@JsonIgnoreProperties(ignoreUnknown = true)
record ScreeningPayload(UUID id, UUID movieId, String movieTitle, Integer availableSeats, Boolean active) {
}
