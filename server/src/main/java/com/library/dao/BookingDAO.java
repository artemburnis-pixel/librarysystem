package com.library.dao;

import com.library.model.Booking;
import com.library.model.BookingIndicatorData;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public void addBooking(Booking booking) {
        String checkReaderSql = "SELECT COUNT(*) FROM readers WHERE id = ?";
        String checkPubSql = "SELECT available_copies FROM publications WHERE id = ?";
        String query = "INSERT INTO bookings " +
                "(reader_id, publication_id, booking_date, expiry_date, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {

            try (PreparedStatement readerStmt = connection.prepareStatement(checkReaderSql)) {
                readerStmt.setInt(1, booking.getReaderId());
                try (ResultSet rs = readerStmt.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        throw new IllegalArgumentException("Читача з ID " + booking.getReaderId() + " не знайдено");
                    }
                }
            }

            try (PreparedStatement pubStmt = connection.prepareStatement(checkPubSql)) {
                pubStmt.setInt(1, booking.getPublicationId());
                try (ResultSet rs = pubStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Видання з ID " + booking.getPublicationId() + " не знайдено");
                    }
                    if (rs.getInt("available_copies") <= 0) {
                        throw new IllegalArgumentException("Немає доступних примірників цього видання для бронювання");
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, booking.getReaderId());
                statement.setInt(2, booking.getPublicationId());

                long bookingMillis = booking.getBookingDate() != null
                        ? booking.getBookingDate().getTime()
                        : System.currentTimeMillis();
                statement.setTimestamp(3, new Timestamp(bookingMillis));

                if (booking.getExpiryDate() != null) {
                    statement.setDate(4,
                            new java.sql.Date(booking.getExpiryDate().getTime()));
                } else {
                    LocalDate defaultExpiry =
                            new java.sql.Date(bookingMillis).toLocalDate().plusDays(3);
                    statement.setDate(4, java.sql.Date.valueOf(defaultExpiry));
                }

                statement.setString(5, booking.getStatus());
                statement.executeUpdate();
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка при додаванні бронювання: " + e.getMessage(), e);
        }
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM bookings";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Booking booking = new Booking();

                booking.setId(resultSet.getInt("id"));
                booking.setReaderId(resultSet.getInt("reader_id"));
                booking.setPublicationId(resultSet.getInt("publication_id"));
                booking.setBookingDate(resultSet.getTimestamp("booking_date"));
                booking.setExpiryDate(resultSet.getDate("expiry_date"));
                booking.setStatus(resultSet.getString("status"));

                bookings.add(booking);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка при отриманні бронювань: " + e.getMessage(), e);
        }

        return bookings;
    }

    public boolean updateBooking(Booking booking) {
        String query = "UPDATE bookings SET " +
                "reader_id = ?, publication_id = ?, booking_date = ?, expiry_date = ? " +
                "WHERE id = ? AND status = 'СТВОРЕНО' " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM issuances WHERE booking_id = bookings.id)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, booking.getReaderId());
            statement.setInt(2, booking.getPublicationId());

            long bookingMillis = booking.getBookingDate() != null
                    ? booking.getBookingDate().getTime()
                    : System.currentTimeMillis();
            statement.setTimestamp(3, new Timestamp(bookingMillis));

            if (booking.getExpiryDate() != null) {
                statement.setDate(4,
                        new java.sql.Date(booking.getExpiryDate().getTime()));
            } else {
                LocalDate defaultExpiry =
                        new java.sql.Date(bookingMillis).toLocalDate().plusDays(3);
                statement.setDate(4, java.sql.Date.valueOf(defaultExpiry));
            }

            statement.setInt(5, booking.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка редагування бронювання: " + e.getMessage(), e);
        }
    }

    public boolean deleteBooking(int id) {
        String query = "DELETE FROM bookings " +
                "WHERE id = ? AND status = 'СТВОРЕНО' " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM issuances WHERE booking_id = bookings.id)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка видалення бронювання: " + e.getMessage(), e);
        }
    }

    public String getStatusById(int id) {
        String query = "SELECT status FROM bookings WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("status");
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка отримання статусу бронювання: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(int id, String oldStatus, String newStatus) {
        String query = "UPDATE bookings SET status = ? " +
                "WHERE id = ? AND status = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newStatus);
            statement.setInt(2, id);
            statement.setString(3, oldStatus);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка зміни статусу бронювання: " + e.getMessage(), e);
        }
    }

    public List<BookingIndicatorData> getAllBookingIndicatorData() {
        List<BookingIndicatorData> dataList = new ArrayList<>();


        String query =
                "SELECT b.id AS booking_id, " +
                        "b.publication_id, " +
                        "p.title AS publication_title, " +
                        "GREATEST(DATEDIFF(CURRENT_TIMESTAMP, b.booking_date), 0) AS wait_days, " +
                        "p.total_copies, " +
                        "p.available_copies, " +
                        "(SELECT COUNT(*) FROM bookings bx " +
                        " WHERE bx.publication_id = b.publication_id " +
                        " AND bx.status <> 'ЗАВЕРШЕНО') AS demand_count " +
                        "FROM bookings b " +
                        "JOIN publications p ON p.id = b.publication_id " +
                        "WHERE b.status <> 'ЗАВЕРШЕНО' " +
                        "ORDER BY b.id";

        String maxDemandQuery =
                "SELECT COALESCE(MAX(demand_count), 0) AS max_demand " +
                        "FROM (" +
                        " SELECT COUNT(*) AS demand_count " +
                        " FROM bookings " +
                        " WHERE status <> 'ЗАВЕРШЕНО' " +
                        " GROUP BY publication_id" +
                        ") AS demands";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            int maxDemand = 0;

            try (Statement maxStatement = connection.createStatement();
                 ResultSet maxResult = maxStatement.executeQuery(maxDemandQuery)) {

                if (maxResult.next()) {
                    maxDemand = maxResult.getInt("max_demand");
                }
            }


            while (resultSet.next()) {
                BookingIndicatorData data = new BookingIndicatorData();
                data.setBookingId(resultSet.getInt("booking_id"));
                data.setPublicationId(resultSet.getInt("publication_id"));
                data.setPublicationTitle(resultSet.getString("publication_title"));
                data.setWaitDays(resultSet.getInt("wait_days"));
                data.setDemandCount(resultSet.getInt("demand_count"));
                data.setMaxDemand(maxDemand);
                data.setTotalCopies(resultSet.getInt("total_copies"));
                data.setAvailableCopies(resultSet.getInt("available_copies"));

                dataList.add(data);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка отримання даних для розрахунку інтегрального показника: " +
                            e.getMessage(), e);
        }

        return dataList;
    }
}