
package com.library.service;

import com.library.dao.BookingDAO;
import com.library.model.BookingIndicator;
import com.library.servlet.BookingIndicatorCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingIndicatorCalculatorTest {

    @Test
    void shouldCalculateAllFactorsCorrectly() {
        BookingIndicator result =
                BookingIndicatorCalculator.calculate(15, 2, 4, 5, 2);

        assertEquals(50.0, result.getF1(), 0.001);
        assertEquals(50.0, result.getF2(), 0.001);
        assertEquals(60.0, result.getF3(), 0.001);
        assertEquals(52.5, result.getIntegralValue(), 0.001);
    }

    @Test
    void shouldLimitWaitDaysFactorTo100() {
        BookingIndicator result =
                BookingIndicatorCalculator.calculate(60, 1, 1, 1, 0);

        assertEquals(100.0, result.getF1(), 0.001);
    }

    @Test
    void shouldReturnZeroDemandFactorWhenMaxDemandIsZero() {
        BookingIndicator result =
                BookingIndicatorCalculator.calculate(0, 0, 0, 5, 5);

        assertEquals(0.0, result.getF2(), 0.001);
    }

    @Test
    void shouldReturnZeroOccupiedCopiesFactorWhenAllCopiesAreAvailable() {
        BookingIndicator result =
                BookingIndicatorCalculator.calculate(0, 0, 0, 5, 5);

        assertEquals(0.0, result.getF3(), 0.001);
    }

    @Test
    void shouldReturn100OccupiedCopiesFactorWhenNoCopiesAreAvailable() {
        BookingIndicator result =
                BookingIndicatorCalculator.calculate(0, 0, 0, 5, 0);

        assertEquals(100.0, result.getF3(), 0.001);
    }

    @Test
    void shouldRejectNegativeValues() {
        assertThrows(IllegalArgumentException.class,
                () -> BookingIndicatorCalculator.calculate(-1, 0, 0, 5, 5));
    }

    @Test
    void shouldRejectAvailableCopiesGreaterThanTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> BookingIndicatorCalculator.calculate(0, 0, 0, 5, 6));
    }

    @Test
    void shouldRejectDemandGreaterThanMaximum() {
        assertThrows(IllegalArgumentException.class,
                () -> BookingIndicatorCalculator.calculate(0, 5, 3, 5, 5));
    }

    @Test
    void shouldAllowCreatedToConfirmedTransition() {
        FakeBookingDAO dao = new FakeBookingDAO("СТВОРЕНО");
        BookingService service = new BookingService(dao);

        service.updateBookingStatus(1, "ПІДТВЕРДЖЕНО");

        assertEquals("ПІДТВЕРДЖЕНО", dao.status);
    }

    @Test
    void shouldAllowConfirmedToReadyTransition() {
        FakeBookingDAO dao = new FakeBookingDAO("ПІДТВЕРДЖЕНО");
        BookingService service = new BookingService(dao);

        service.updateBookingStatus(1, "ГОТОВЕ_ДО_ВИДАЧІ");

        assertEquals("ГОТОВЕ_ДО_ВИДАЧІ", dao.status);
    }

    @Test
    void shouldAllowReadyToCompletedTransition() {
        FakeBookingDAO dao = new FakeBookingDAO("ГОТОВЕ_ДО_ВИДАЧІ");
        BookingService service = new BookingService(dao);

        service.updateBookingStatus(1, "ЗАВЕРШЕНО");

        assertEquals("ЗАВЕРШЕНО", dao.status);
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        FakeBookingDAO dao = new FakeBookingDAO("СТВОРЕНО");
        BookingService service = new BookingService(dao);

        assertThrows(IllegalStateException.class,
                () -> service.updateBookingStatus(1, "ГОТОВЕ_ДО_ВИДАЧІ"));

        assertEquals("СТВОРЕНО", dao.status);
    }

    @Test
    void shouldRejectTransitionFromCompletedStatus() {
        FakeBookingDAO dao = new FakeBookingDAO("ЗАВЕРШЕНО");
        BookingService service = new BookingService(dao);

        assertThrows(IllegalStateException.class,
                () -> service.updateBookingStatus(1, "СТВОРЕНО"));
    }

    private static class FakeBookingDAO extends BookingDAO {
        private String status;

        FakeBookingDAO(String status) {
            this.status = status;
        }

        @Override
        public String getStatusById(int id) {
            return status;
        }

        @Override
        public boolean updateStatus(int id, String oldStatus, String newStatus) {
            if (!status.equals(oldStatus)) {
                return false;
            }

            status = newStatus;
            return true;
        }
    }
}
