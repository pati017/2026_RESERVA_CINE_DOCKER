package com.cinema.movie_service.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScreeningRepositoryPort {
    Screening save(Screening screening);
    Optional<Screening> findById(UUID id);

    /** Igual que findById pero con bloqueo de escritura: evita sobreventa bajo concurrencia. */
    Optional<Screening> findByIdForUpdate(UUID id);

    List<Screening> findAll();
}
