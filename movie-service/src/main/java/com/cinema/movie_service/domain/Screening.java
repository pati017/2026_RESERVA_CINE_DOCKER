package com.cinema.movie_service.domain;

import com.cinema.movie_service.exception.NoSeatsAvailableException;
import com.cinema.movie_service.exception.ScreeningNotAvailableException;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Agregado Screening (función). Invariantes que el propio dominio garantiza:
 * <ul>
 *   <li>0 &lt;= availableSeats &lt;= totalSeats</li>
 *   <li>no se reserva en una función inactiva</li>
 * </ul>
 */
@Getter
@Builder
public class Screening {
    private UUID id;
    private UUID movieId;
    private String room;
    private LocalDateTime showAt;
    private int totalSeats;
    private int availableSeats;
    @Builder.Default
    private boolean active = true;

    /** Factory: una función nueva nace con todos sus asientos disponibles. */
    public static Screening open(UUID movieId, String room, LocalDateTime showAt, int totalSeats) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("totalSeats debe ser mayor que cero");
        }
        return Screening.builder()
                .movieId(movieId)
                .room(room)
                .showAt(showAt)
                .totalSeats(totalSeats)
                .availableSeats(totalSeats)
                .active(true)
                .build();
    }

    /** Descuenta un asiento. Lanza excepción de dominio (→ 422) si no se puede. */
    public void reserve() {
        if (!active) {
            throw new ScreeningNotAvailableException("La función no está activa");
        }
        if (availableSeats <= 0) {
            throw new NoSeatsAvailableException("Función sin asientos disponibles");
        }
        availableSeats--;
    }

    /** Libera un asiento (compensación del SAGA). Nunca supera la capacidad total. */
    public void release() {
        if (availableSeats < totalSeats) {
            availableSeats++;
        }
    }
}
