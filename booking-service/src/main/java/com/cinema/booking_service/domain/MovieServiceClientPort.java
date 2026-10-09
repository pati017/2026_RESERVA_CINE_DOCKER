package com.cinema.booking_service.domain;

import java.util.Optional;
import java.util.UUID;

/** Puerto de salida hacia movie-service. La implementación HTTP vive en infrastructure/client. */
public interface MovieServiceClientPort {

    /** Optional.empty() si la función no existe. Lanza MovieServiceUnavailableException si movie-service falla. */
    Optional<ScreeningInfo> findScreening(UUID screeningId);

    void reserveSeat(UUID screeningId);

    void releaseSeat(UUID screeningId);
}
