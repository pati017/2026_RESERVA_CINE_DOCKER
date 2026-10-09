package com.cinema.movie_service.domain;

import com.cinema.movie_service.exception.NoSeatsAvailableException;
import com.cinema.movie_service.exception.ScreeningNotAvailableException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Invariantes del dominio: se prueban sin Spring ni base de datos. */
class ScreeningTest {

    private Screening screening(int total) {
        return Screening.open(UUID.randomUUID(), "Sala 1", LocalDateTime.now().plusDays(1), total);
    }

    @Test
    void openStartsWithAllSeatsAvailable() {
        Screening s = screening(45);
        assertEquals(45, s.getTotalSeats());
        assertEquals(45, s.getAvailableSeats());
    }

    @Test
    void reserveDecrementsAvailableSeats() {
        Screening s = screening(2);
        s.reserve();
        assertEquals(1, s.getAvailableSeats());
    }

    @Test
    void reserveWithoutSeatsThrowsAndNeverGoesNegative() {
        Screening s = screening(1);
        s.reserve();
        assertThrows(NoSeatsAvailableException.class, s::reserve);
        assertEquals(0, s.getAvailableSeats());
    }

    @Test
    void reserveOnInactiveScreeningIsRejected() {
        Screening s = Screening.builder().totalSeats(10).availableSeats(10).active(false).build();
        assertThrows(ScreeningNotAvailableException.class, s::reserve);
        assertEquals(10, s.getAvailableSeats());
    }

    @Test
    void releaseIncrementsButNeverExceedsCapacity() {
        Screening s = screening(2);
        s.reserve();
        s.release();
        s.release(); // ya estaba lleno: no debe pasar de totalSeats
        assertEquals(2, s.getAvailableSeats());
    }

    @Test
    void openRejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> screening(0));
    }
}
