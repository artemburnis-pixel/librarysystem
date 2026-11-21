package com.library.service;

import com.library.dao.BookingDAO;
import com.library.dao.PublicationDAO;
import com.library.dao.ReaderDAO;
import com.library.model.Booking;
import com.library.model.BookingIndicator;
import com.library.model.Publication;
import com.library.model.Reader;
import org.junit.jupiter.api.Test;

import java.util.Date;

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

    @Test
    void shouldRejectPublicationWithEmptyTitle() {
        PublicationService service = new PublicationService(new FakePublicationDAO());
        Publication invalid = new Publication("   ", "Автор", 2020, 5, 5);

        assertThrows(IllegalArgumentException.class,
                () -> service.addPublication(invalid));
    }

    @Test
    void shouldRejectPublicationWithAvailableGreaterThanTotal() {
        PublicationService service = new PublicationService(new FakePublicationDAO());
        Publication invalid = new Publication("Книга", "Автор", 2020, 3, 4);

        assertThrows(IllegalArgumentException.class,
                () -> service.addPublication(invalid));
    }

    @Test
    void shouldAcceptValidPublication() {
        FakePublicationDAO dao = new FakePublicationDAO();
        PublicationService service = new PublicationService(dao);
        Publication valid = new Publication("Книга", "Автор", 2020, 5, 3);

        service.addPublication(valid);

        assertTrue(dao.added);
    }

    @Test
    void shouldRejectReaderWithBlankFullName() {
        ReaderService service = new ReaderService(new FakeReaderDAO());
        Reader invalid = new Reader(0, "", "test@mail.com", "+380501112233", "Адреса", null);

        assertThrows(IllegalArgumentException.class,
                () -> service.addReader(invalid));
    }

    @Test
    void shouldRejectReaderWithInvalidEmail() {
        ReaderService service = new ReaderService(new FakeReaderDAO());
        Reader invalid = new Reader(0, "Іван Петренко", "invalid-email", "+380501112233", "Адреса", null);

        assertThrows(IllegalArgumentException.class,
                () -> service.addReader(invalid));
    }

    @Test
    void shouldRejectBookingWithInvalidReaderOrPublicationId() {
        BookingService service = new BookingService(new FakeBookingDAO("СТВОРЕНО"));
        Booking invalid = new Booking(0, 1, new Date(), null, "СТВОРЕНО");

        assertThrows(IllegalArgumentException.class,
                () -> service.addBooking(invalid));
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

    private static class FakePublicationDAO extends PublicationDAO {
        private boolean added = false;

        @Override
        public void addPublication(Publication publication) {
            this.added = true;
        }
    }

    private static class FakeReaderDAO extends ReaderDAO {
        @Override
        public void addReader(Reader reader) {
        }
    }
}