package com.library.service;

import com.library.dao.BookingDAO;
import com.library.model.Booking;
import com.library.model.BookingIndicator;
import com.library.model.BookingIndicatorData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BookingService {

    private static final Logger LOGGER =
            Logger.getLogger(BookingService.class.getName());

    private final BookingDAO bookingDAO;

    private static final String CREATED = "СТВОРЕНО";
    private static final String CONFIRMED = "ПІДТВЕРДЖЕНО";
    private static final String READY = "ГОТОВЕ_ДО_ВИДАЧІ";
    private static final String COMPLETED = "ЗАВЕРШЕНО";

    public BookingService() {
        this(new BookingDAO());
    }

    public BookingService(BookingDAO bookingDAO) {
        if (bookingDAO == null) {
            throw new IllegalArgumentException(
                    "BookingDAO не може бути null");
        }
        this.bookingDAO = bookingDAO;
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.getAllBookings();
    }

    public List<BookingIndicator> getAllBookingIndicators() {
        List<BookingIndicatorData> rawData = bookingDAO.getAllBookingIndicatorData();
        List<BookingIndicator> indicators = new ArrayList<>();

        for (BookingIndicatorData item : rawData) {
            BookingIndicator indicator = BookingIndicatorCalculator.calculate(
                    item.getWaitDays(),
                    item.getDemandCount(),
                    item.getMaxDemand(),
                    item.getTotalCopies(),
                    item.getAvailableCopies()
            );
            indicator.setBookingId(item.getBookingId());
            indicator.setPublicationId(item.getPublicationId());
            indicator.setPublicationTitle(item.getPublicationTitle());
            indicators.add(indicator);
        }

        LOGGER.info("Розраховано інтегральні показники для " + indicators.size() + " активних бронювань");
        return indicators;
    }

    public void addBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException(
                    "Бронювання не може бути порожнім");
        }

        if (booking.getReaderId() <= 0 ||
                booking.getPublicationId() <= 0) {
            throw new IllegalArgumentException(
                    "Некоректний ID читача або видання");
        }

        booking.setStatus(CREATED);
        bookingDAO.addBooking(booking);
        LOGGER.info("Створено нове бронювання: readerId=" + booking.getReaderId()
                + ", publicationId=" + booking.getPublicationId());
    }

    public void updateBookingStatus(int id, String newStatus) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Некоректний ID бронювання");
        }

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Новий статус не може бути порожнім");
        }

        String currentStatus = bookingDAO.getStatusById(id);

        if (currentStatus == null) {
            throw new IllegalArgumentException(
                    "Бронювання з ID " + id + " не знайдено");
        }

        String expectedNextStatus;

        switch (currentStatus) {
            case CREATED:
                expectedNextStatus = CONFIRMED;
                break;

            case CONFIRMED:
                expectedNextStatus = READY;
                break;

            case READY:
                expectedNextStatus = COMPLETED;
                break;

            case COMPLETED:
                throw new IllegalStateException(
                        "Бронювання вже завершене");

            default:
                throw new IllegalStateException(
                        "Невідомий поточний статус: " + currentStatus);
        }

        if (!expectedNextStatus.equals(newStatus)) {
            LOGGER.warning("Спроба недозволеного переходу статусу для бронювання ID=" + id
                    + ": " + currentStatus + " -> " + newStatus);
            throw new IllegalStateException(
                    "Недозволений перехід зі статусу " + currentStatus +
                            " у статус " + newStatus +
                            ". Наступний дозволений статус: " +
                            expectedNextStatus);
        }

        boolean updated = bookingDAO.updateStatus(
                id, currentStatus, newStatus);

        if (!updated) {
            throw new IllegalStateException(
                    "Статус бронювання вже змінився. Оновіть список.");
        }

        LOGGER.info("Статус бронювання ID=" + id + " змінено: " + currentStatus + " -> " + newStatus);
    }

    public void updateBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException(
                    "Бронювання не може бути порожнім");
        }

        if (booking.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Некоректний ID бронювання");
        }

        if (booking.getReaderId() <= 0 ||
                booking.getPublicationId() <= 0) {
            throw new IllegalArgumentException(
                    "Некоректний ID читача або видання");
        }

        String currentStatus = bookingDAO.getStatusById(booking.getId());

        if (currentStatus == null) {
            throw new IllegalArgumentException(
                    "Бронювання не знайдено");
        }

        if (!CREATED.equals(currentStatus)) {
            throw new IllegalStateException(
                    "Редагувати можна тільки бронювання зі статусом СТВОРЕНО");
        }

        boolean updated = bookingDAO.updateBooking(booking);

        if (!updated) {
            throw new IllegalStateException(
                    "Не вдалося відредагувати бронювання");
        }

        LOGGER.info("Відредаговано бронювання ID=" + booking.getId());
    }

    public void deleteBooking(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Некоректний ID бронювання");
        }

        String currentStatus = bookingDAO.getStatusById(id);

        if (currentStatus == null) {
            throw new IllegalArgumentException(
                    "Бронювання не знайдено");
        }

        if (!CREATED.equals(currentStatus)) {
            throw new IllegalStateException(
                    "Видаляти можна тільки бронювання зі статусом СТВОРЕНО");
        }

        boolean deleted = bookingDAO.deleteBooking(id);

        if (!deleted) {
            throw new IllegalStateException(
                    "Не вдалося видалити бронювання.");
        }

        LOGGER.info("Видалено бронювання ID=" + id);
    }
}