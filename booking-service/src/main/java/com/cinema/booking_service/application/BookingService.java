package com.cinema.booking_service.application;

import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.domain.BookingConfirmedEvent;
import com.cinema.booking_service.domain.BookingEventPublisherPort;
import com.cinema.booking_service.domain.BookingRepositoryPort;
import com.cinema.booking_service.domain.MovieServiceClientPort;
import com.cinema.booking_service.domain.ScreeningInfo;
import com.cinema.booking_service.exception.BookingAccessDeniedException;
import com.cinema.booking_service.exception.MovieServiceUnavailableException;
import com.cinema.booking_service.exception.NoSeatsAvailableException;
import com.cinema.booking_service.exception.ResourceNotFoundException;
import com.cinema.booking_service.exception.ScreeningUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Orquestador del SAGA (sin two-phase commit).
 *
 * Importante: NO hay @Transactional en estos métodos. Cada save() del repositorio es su propia
 * transacción local; una transacción abierta durante llamadas HTTP retendría la conexión a la BD y,
 * si fallara, revertiría también la compensación (el booking CANCELLED desaparecería).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepositoryPort repositoryPort;
    private final MovieServiceClientPort movieClient;
    private final BookingEventPublisherPort eventPublisher;

    // ═══════════════════════════ SAGA de reserva — POST /bookings ═══════════════════════════

    public Booking createBooking(UUID screeningId, UUID customerId, BigDecimal totalAmount) {
        // Paso 1: verificar función disponible. Si falla o no hay asientos → 422 sin crear nada.
        ScreeningInfo screening = verifyScreening(screeningId);

        UUID bookingId = null;
        boolean seatReserved = false;
        Booking confirmed;

        try {
            // Paso 2: booking PENDING (con snapshot del título). Se anota el id DESPUÉS de persistir.
            Booking pending = repositoryPort.save(
                    Booking.pending(screeningId, screening.movieTitle(), customerId, totalAmount));
            bookingId = pending.getId();

            // Paso 3: reservar asiento en movie-service (@Retry: 3 intentos / 500 ms).
            movieClient.reserveSeat(screeningId);
            seatReserved = true; // ← solo DESPUÉS de que el paso terminó bien

            // Paso 4: confirmar.
            pending.confirm();
            confirmed = repositoryPort.save(pending);

        } catch (RuntimeException e) {
            log.error("SAGA de reserva falló (bookingId={}, seatReserved={}): {}", bookingId, seatReserved, e.getMessage());
            if (seatReserved) {
                compensate("release_seat", () -> movieClient.releaseSeat(screeningId));
            }
            if (bookingId != null) {
                final UUID idToCancel = bookingId;
                compensate("cancel_booking", () -> cancelLocally(idToCancel));
            }
            throw e; // el cliente recibe el error real (422 / 503 / 502...), y la reserva nunca queda PENDING
        }

        // Extra Kafka: se publica FUERA del try del SAGA. Si fallara, jamás debe cancelar una reserva ya confirmada.
        publishConfirmed(confirmed);
        return confirmed;
    }

    private void publishConfirmed(Booking confirmed) {
        try {
            eventPublisher.publishBookingConfirmed(BookingConfirmedEvent.from(confirmed));
        } catch (RuntimeException ex) {
            log.error("Fallo al publicar BookingConfirmedEvent {} (se ignora, la reserva sigue CONFIRMED): {}",
                    confirmed.getId(), ex.getMessage());
        }
    }

    private ScreeningInfo verifyScreening(UUID screeningId) {
        ScreeningInfo screening;
        try {
            screening = movieClient.findScreening(screeningId)
                    .orElseThrow(() -> new ScreeningUnavailableException("La función " + screeningId + " no existe"));
        } catch (MovieServiceUnavailableException e) {
            throw new ScreeningUnavailableException("No se pudo verificar la función: " + e.getMessage());
        }
        if (!screening.active()) {
            throw new ScreeningUnavailableException("La función no está activa");
        }
        if (!screening.hasAvailableSeats()) {
            throw new NoSeatsAvailableException("Función sin asientos disponibles");
        }
        return screening;
    }

    // ═══════════════════════════ SAGA de cancelación — PATCH /bookings/{id}/cancel ═══════════════════════════

    public Booking cancelBooking(UUID id, Requester requester) {
        // Paso 1: existe (404), es del usuario (403) y puede cancelarse (422 si ya está CANCELLED).
        Booking booking = getById(id, requester);
        booking.cancel();

        // Paso 2: CANCELLED local.
        Booking saved = repositoryPort.save(booking);

        // Paso 3: liberar asiento — best-effort. Si falla se loguea; la reserva queda CANCELLED igual.
        compensate("release_seat", () -> movieClient.releaseSeat(saved.getScreeningId()));
        return saved;
    }

    // ═══════════════════════════ Consulta ═══════════════════════════

    public Booking getById(UUID id, Requester requester) {
        Booking booking = repositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        if (!requester.admin() && !booking.belongsTo(requester.customerId())) {
            throw new BookingAccessDeniedException("La reserva pertenece a otro usuario");
        }
        return booking;
    }

    // ═══════════════════════════ Utilidades del SAGA ═══════════════════════════

    /** Compensación best-effort: absorbe cualquier excepción para no tapar el error original. */
    private void compensate(String step, Runnable action) {
        try {
            action.run();
            log.info("Compensación '{}' ejecutada", step);
        } catch (RuntimeException ex) {
            log.error("Compensación '{}' falló (best-effort, se ignora): {}", step, ex.getMessage());
        }
    }

    private void cancelLocally(UUID bookingId) {
        repositoryPort.findById(bookingId).ifPresent(b -> {
            b.cancel();
            repositoryPort.save(b);
        });
    }
}
