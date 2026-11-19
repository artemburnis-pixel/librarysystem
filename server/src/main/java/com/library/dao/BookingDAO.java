
package com.library.dao;

import com.library.model.Booking;
import com.library.model.BookingIndicator;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public void addBooking(Booking booking) {
        String query = "INSERT INTO bookings " +
                "(reader_id, publication_id, booking_date, status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, booking.getReaderId());
            statement.setInt(2, booking.getPublicationId());

            if (booking.getBookingDate() != null) {
                statement.setTimestamp(3,
                        new Timestamp(booking.getBookingDate().getTime()));
            } else {
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }

            statement.setString(4, booking.getStatus());
            statement.executeUpdate();

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
                "reader_id = ?, publication_id = ?, booking_date = ? " +
                "WHERE id = ? AND status = 'СТВОРЕНО' " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM issuances WHERE booking_id = bookings.id)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, booking.getReaderId());
            statement.setInt(2, booking.getPublicationId());

            if (booking.getBookingDate() != null) {
                statement.setTimestamp(3,
                        new Timestamp(booking.getBookingDate().getTime()));
            } else {
                statement.setTimestamp(3,
                        new Timestamp(System.currentTimeMillis()));
            }

            statement.setInt(4, booking.getId());

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

    public List<BookingIndicator> getAllBookingIndicators() {
        List<BookingIndicator> indicators = new ArrayList<>();

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
                BookingIndicator indicator = new BookingIndicator();

                int waitDays = resultSet.getInt("wait_days");
                int demandCount = resultSet.getInt("demand_count");
                int totalCopies = resultSet.getInt("total_copies");
                int availableCopies = resultSet.getInt("available_copies");

                double f1 = Math.min(waitDays / 30.0, 1.0) * 100.0;

                double f2 = maxDemand > 0
                        ? (demandCount / (double) maxDemand) * 100.0
                        : 0.0;

                double f3 = totalCopies > 0
                        ? ((totalCopies - availableCopies) / (double) totalCopies) * 100.0
                        : 0.0;

                f1 = Math.max(0.0, Math.min(f1, 100.0));
                f2 = Math.max(0.0, Math.min(f2, 100.0));
                f3 = Math.max(0.0, Math.min(f3, 100.0));

                double integralValue =
                        0.40 * f1 +
                                0.35 * f2 +
                                0.25 * f3;

                indicator.setBookingId(resultSet.getInt("booking_id"));
                indicator.setPublicationId(resultSet.getInt("publication_id"));
                indicator.setPublicationTitle(
                        resultSet.getString("publication_title"));

                indicator.setWaitDays(waitDays);
                indicator.setF1(f1);

                indicator.setDemandCount(demandCount);
                indicator.setF2(f2);

                indicator.setTotalCopies(totalCopies);
                indicator.setAvailableCopies(availableCopies);
                indicator.setF3(f3);

                indicator.setIntegralValue(integralValue);

                indicators.add(indicator);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка розрахунку інтегрального показника: " +
                            e.getMessage(), e);
        }

        return indicators;
    }
}
