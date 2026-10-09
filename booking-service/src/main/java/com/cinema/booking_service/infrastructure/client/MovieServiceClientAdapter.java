package com.cinema.booking_service.infrastructure.client;

import com.cinema.booking_service.domain.MovieServiceClientPort;
import com.cinema.booking_service.domain.ScreeningInfo;
import com.cinema.booking_service.exception.MovieServiceCircuitOpenException;
import com.cinema.booking_service.exception.MovieServiceRejectedException;
import com.cinema.booking_service.exception.MovieServiceUnavailableException;
import com.cinema.booking_service.exception.NoSeatsAvailableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador HTTP hacia movie-service. Traduce errores HTTP a excepciones propias para que el
 * SAGA decida qué reintentar y qué compensar:
 *  - 422                    → NoSeatsAvailableException       (negocio, NO se reintenta)
 *  - otros 4xx              → MovieServiceRejectedException   (NO se reintenta)
 *  - 5xx / timeout / sin instancia → MovieServiceUnavailableException (transitorio, SÍ se reintenta)
 *
 * Extra Circuit Breaker ("movieService"): si movie-service falla de forma sostenida el circuito se abre
 * y cada método usa su fallback (MovieServiceCircuitOpenException → 503) sin esperar timeouts.
 * Orden de los aspectos de Resilience4j: Retry envuelve a CircuitBreaker (cada intento cuenta en el circuito).
 */
@Component
@Slf4j
public class MovieServiceClientAdapter implements MovieServiceClientPort {

    private final RestClient movieServiceClient;

    public MovieServiceClientAdapter(RestClient movieServiceClient) {
        this.movieServiceClient = movieServiceClient;
    }

    @Override
    @CircuitBreaker(name = "movieService", fallbackMethod = "findScreeningFallback")
    public Optional<ScreeningInfo> findScreening(UUID screeningId) {
        try {
            ScreeningPayload p = movieServiceClient.get()
                    .uri("/screenings/{id}", screeningId)
                    .retrieve()
                    .body(ScreeningPayload.class);
            if (p == null) {
                return Optional.empty();
            }
            return Optional.of(new ScreeningInfo(p.id(), p.movieId(), p.movieTitle(),
                    p.availableSeats() == null ? 0 : p.availableSeats(),
                    p.active() == null || p.active()));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            throw new MovieServiceRejectedException("movie-service respondió " + e.getStatusCode() + " al consultar la función");
        } catch (RestClientException | IllegalStateException e) {
            throw new MovieServiceUnavailableException("No se pudo consultar movie-service: " + e.getMessage());
        }
    }

    /** Paso 3 del SAGA. Config en application.yml → resilience4j.retry.instances.reserveSeat (3 intentos, 500 ms). */
    @Override
    @Retry(name = "reserveSeat")
    @CircuitBreaker(name = "movieService", fallbackMethod = "reserveSeatFallback")
    public void reserveSeat(UUID screeningId) {
        log.info("Reservando asiento en movie-service para la función {}", screeningId);
        try {
            movieServiceClient.patch()
                    .uri("/screenings/{id}/reserve", screeningId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.UnprocessableEntity e) {
            throw new NoSeatsAvailableException("Función sin asientos disponibles");
        } catch (HttpClientErrorException e) {
            throw new MovieServiceRejectedException("movie-service respondió " + e.getStatusCode() + " al reservar el asiento");
        } catch (RestClientException | IllegalStateException e) {
            log.warn("Fallo transitorio al reservar asiento ({}): {}", screeningId, e.getMessage());
            throw new MovieServiceUnavailableException("movie-service no disponible: " + e.getMessage());
        }
    }

    @Override
    @CircuitBreaker(name = "movieService", fallbackMethod = "releaseSeatFallback")
    public void releaseSeat(UUID screeningId) {
        log.info("Liberando asiento en movie-service para la función {}", screeningId);
        try {
            movieServiceClient.patch()
                    .uri("/screenings/{id}/release", screeningId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            throw new MovieServiceRejectedException("movie-service respondió " + e.getStatusCode() + " al liberar el asiento");
        } catch (RestClientException | IllegalStateException e) {
            throw new MovieServiceUnavailableException("movie-service no disponible: " + e.getMessage());
        }
    }

    // ───────────────────────── Fallbacks del Circuit Breaker ─────────────────────────
    // Solo se activan con el circuito ABIERTO (CallNotPermittedException). Los demás errores siguen su camino
    // normal (422, 502, 503) para que el SAGA decida qué compensar.

    @SuppressWarnings("unused")
    private Optional<ScreeningInfo> findScreeningFallback(UUID screeningId, CallNotPermittedException ex) {
        log.warn("Circuit Breaker ABIERTO: no se consulta la función {}", screeningId);
        throw new MovieServiceCircuitOpenException("movie-service no disponible temporalmente (circuito abierto). Intenta en unos segundos.");
    }

    @SuppressWarnings("unused")
    private void reserveSeatFallback(UUID screeningId, CallNotPermittedException ex) {
        log.warn("Circuit Breaker ABIERTO: no se reserva asiento de la función {}", screeningId);
        throw new MovieServiceCircuitOpenException("movie-service no disponible temporalmente (circuito abierto). Intenta en unos segundos.");
    }

    @SuppressWarnings("unused")
    private void releaseSeatFallback(UUID screeningId, CallNotPermittedException ex) {
        log.warn("Circuit Breaker ABIERTO: no se libera asiento de la función {} (best-effort)", screeningId);
        throw new MovieServiceCircuitOpenException("movie-service no disponible temporalmente (circuito abierto)");
    }
}
