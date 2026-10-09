package com.cinema.booking_service.domain;

import com.cinema.booking_service.exception.InvalidBookingStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingTest {

    private Booking pending() {
        return Booking.pending(UUID.randomUUID(), "Toy Story", UUID.randomUUID(), new BigDecimal("15.50"));
    }

    @Test
    void newBookingIsPendingAndKeepsTitleSnapshot() {
        Booking b = pending();
        assertEquals(BookingStatus.PENDING, b.getStatus());
        assertEquals("Toy Story", b.getMovieTitle());
    }

    @Test
    void confirmMovesPendingToConfirmed() {
        Booking b = pending();
        b.confirm();
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
    }

    @Test
    void cannotConfirmTwice() {
        Booking b = pending();
        b.confirm();
        assertThrows(InvalidBookingStateException.class, b::confirm);
    }

    @Test
    void cancelWorksOnceAndThenIsRejected() {
        Booking b = pending();
        b.confirm();
        b.cancel();
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertThrows(InvalidBookingStateException.class, b::cancel);
    }

    @Test
    void belongsToComparesCustomer() {
        Booking b = pending();
        assertTrue(b.belongsTo(b.getCustomerId()));
    }
}
