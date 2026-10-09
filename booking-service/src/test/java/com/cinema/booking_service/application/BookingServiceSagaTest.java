package com.cinema.booking_service.application;

import com.cinema.booking_service.domain.Booking;
import com.cinema.booking_service.domain.BookingConfirmedEvent;
import com.cinema.booking_service.domain.BookingEventPublisherPort;
import com.cinema.booking_service.domain.BookingRepositoryPort;
import com.cinema.booking_service.domain.BookingStatus;
import com.cinema.booking_service.domain.MovieServiceClientPort;
import com.cinema.booking_service.domain.ScreeningInfo;
import com.cinema.booking_service.exception.BookingAccessDeniedException;
import com.cinema.booking_service.exception.InvalidBookingStateException;
import com.cinema.booking_service.exception.MovieServiceUnavailableException;
import com.cinema.booking_service.exception.NoSeatsAvailableException;
import com.cinema.booking_service.exception.ScreeningUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el orquestador del SAGA con dobles en memoria (sin Mockito, sin Spring, sin BD):
 * reserva exitosa, sin asientos (422), fallo del paso 3, fallo del paso 4 y cancelación best-effort.
 */
class BookingServiceSagaTest {

    private final UUID screeningId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final BigDecimal amount = new BigDecimal("15.50");

    private InMemoryBookings bookings;
    private FakeMovieClient movie;
    private FakePublisher events;
    private BookingService service;

    @BeforeEach
    void setUp() {
        bookings = new InMemoryBookings();
        movie = new FakeMovieClient(screeningId);
        events = new FakePublisher();
        service = new BookingService(bookings, movie, events);
    }

    @Test
    void happyPathConfirmsBookingAndReservesOneSeat() {
        Booking b = service.createBooking(screeningId, customerId, amount);

        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
        assertEquals("Toy Story", b.getMovieTitle()); // snapshot real, no un valor fijo
        assertEquals(1, movie.reserveCalls);
        assertEquals(0, movie.releaseCalls);
    }

    @Test
    void successfulBookingPublishesOneConfirmedEvent() {
        Booking b = service.createBooking(screeningId, customerId, amount);

        assertEquals(1, events.published.size());
        assertEquals(b.getId(), events.published.get(0).bookingId());
        assertEquals("Toy Story", events.published.get(0).movieTitle());
    }

    @Test
    void failedSagaPublishesNoEvent() {
        movie.failReserve = true;
        assertThrows(MovieServiceUnavailableException.class, () -> service.createBooking(screeningId, customerId, amount));
        assertTrue(events.published.isEmpty());
    }

    @Test
    void publisherFailureNeverCancelsAConfirmedBooking() {
        events.fail = true;

        Booking b = service.createBooking(screeningId, customerId, amount);

        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
        assertEquals(0, movie.releaseCalls); // sin compensación: el asiento sigue reservado
    }

    @Test
    void noSeatsFailsFastWithoutCreatingAnyBooking() {
        movie.availableSeats = 0;

        assertThrows(NoSeatsAvailableException.class, () -> service.createBooking(screeningId, customerId, amount));

        assertTrue(bookings.store.isEmpty());
        assertEquals(0, movie.reserveCalls);
    }

    @Test
    void unknownScreeningFailsWith422TypeException() {
        movie.screeningExists = false;
        assertThrows(ScreeningUnavailableException.class, () -> service.createBooking(screeningId, customerId, amount));
        assertTrue(bookings.store.isEmpty());
    }

    @Test
    void movieServiceDownOnVerifyIsReportedAsScreeningUnavailable() {
        movie.failFind = true;
        assertThrows(ScreeningUnavailableException.class, () -> service.createBooking(screeningId, customerId, amount));
    }

    @Test
    void reserveFailureCancelsBookingAndDoesNotReleaseSeat() {
        movie.failReserve = true;

        assertThrows(MovieServiceUnavailableException.class, () -> service.createBooking(screeningId, customerId, amount));

        assertEquals(1, bookings.store.size());
        assertEquals(BookingStatus.CANCELLED, bookings.store.values().iterator().next().getStatus());
        assertEquals(0, movie.releaseCalls); // el asiento nunca se reservó → nada que liberar
    }

    @Test
    void confirmFailureReleasesSeatAndCancelsBooking() {
        bookings.failSaveOnNthCall = 2; // 1ª save = PENDING (ok), 2ª save = CONFIRMED (falla)

        assertThrows(IllegalStateException.class, () -> service.createBooking(screeningId, customerId, amount));

        assertEquals(1, movie.releaseCalls);
        assertEquals(BookingStatus.CANCELLED, bookings.store.values().iterator().next().getStatus());
    }

    @Test
    void failingCompensationIsAbsorbedAndOriginalErrorSurvives() {
        bookings.failSaveOnNthCall = 2;
        movie.failRelease = true;

        // La liberación falla, pero lo que llega al cliente es el error original, no el de la compensación.
        assertThrows(IllegalStateException.class, () -> service.createBooking(screeningId, customerId, amount));
        assertEquals(1, movie.releaseCalls);
    }

    @Test
    void cancelMarksCancelledAndReleasesSeat() {
        Booking created = service.createBooking(screeningId, customerId, amount);
        Requester owner = new Requester(customerId, false);

        Booking cancelled = service.cancelBooking(created.getId(), owner);

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
        assertEquals(1, movie.releaseCalls);
    }

    @Test
    void cancelStaysCancelledEvenIfReleaseFails() {
        Booking created = service.createBooking(screeningId, customerId, amount);
        movie.failRelease = true;

        Booking cancelled = service.cancelBooking(created.getId(), new Requester(customerId, false));

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus()); // best-effort: NO se revierte
    }

    @Test
    void cancellingTwiceIsRejected() {
        Booking created = service.createBooking(screeningId, customerId, amount);
        Requester owner = new Requester(customerId, false);
        service.cancelBooking(created.getId(), owner);

        assertThrows(InvalidBookingStateException.class, () -> service.cancelBooking(created.getId(), owner));
    }

    @Test
    void otherUserCannotReadOrCancelButAdminCan() {
        Booking created = service.createBooking(screeningId, customerId, amount);
        Requester stranger = new Requester(UUID.randomUUID(), false);
        Requester admin = new Requester(UUID.randomUUID(), true);

        assertThrows(BookingAccessDeniedException.class, () -> service.getById(created.getId(), stranger));
        assertThrows(BookingAccessDeniedException.class, () -> service.cancelBooking(created.getId(), stranger));
        assertEquals(created.getId(), service.getById(created.getId(), admin).getId());
    }

    // ───────────────────────── dobles de prueba ─────────────────────────

    static class InMemoryBookings implements BookingRepositoryPort {
        final Map<UUID, Booking> store = new HashMap<>();
        int saveCalls = 0;
        int failSaveOnNthCall = -1;

        @Override
        public Booking save(Booking booking) {
            saveCalls++;
            if (saveCalls == failSaveOnNthCall) {
                throw new IllegalStateException("fallo simulado de base de datos");
            }
            // Las compensaciones también hacen save: dejamos que pasen tras el fallo simulado.
            Booking stored = booking.getId() == null
                    ? Booking.builder().id(UUID.randomUUID()).screeningId(booking.getScreeningId())
                        .movieTitle(booking.getMovieTitle()).customerId(booking.getCustomerId())
                        .status(booking.getStatus()).totalAmount(booking.getTotalAmount()).build()
                    : booking;
            store.put(stored.getId(), stored);
            return stored;
        }

        @Override
        public Optional<Booking> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    static class FakePublisher implements BookingEventPublisherPort {
        final java.util.List<BookingConfirmedEvent> published = new java.util.ArrayList<>();
        boolean fail;

        @Override
        public void publishBookingConfirmed(BookingConfirmedEvent event) {
            if (fail) throw new IllegalStateException("Kafka caído");
            published.add(event);
        }
    }

    static class FakeMovieClient implements MovieServiceClientPort {
        final UUID screeningId;
        int availableSeats = 10;
        boolean screeningExists = true;
        boolean failFind, failReserve, failRelease;
        int reserveCalls, releaseCalls;

        FakeMovieClient(UUID screeningId) {
            this.screeningId = screeningId;
        }

        @Override
        public Optional<ScreeningInfo> findScreening(UUID id) {
            if (failFind) throw new MovieServiceUnavailableException("movie-service caído");
            if (!screeningExists) return Optional.empty();
            return Optional.of(new ScreeningInfo(id, UUID.randomUUID(), "Toy Story", availableSeats, true));
        }

        @Override
        public void reserveSeat(UUID id) {
            if (failReserve) throw new MovieServiceUnavailableException("movie-service caído");
            reserveCalls++;
        }

        @Override
        public void releaseSeat(UUID id) {
            releaseCalls++;
            if (failRelease) throw new MovieServiceUnavailableException("movie-service caído");
        }
    }
}
