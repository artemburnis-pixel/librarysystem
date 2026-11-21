package com.library.dao;

import com.library.model.Issuance;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssuanceDAO {

    public boolean bookingExists(int bookingId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка перевірки бронювання: " + e.getMessage(), e);
        }
    }

    public boolean issuanceExists(int bookingId) {
        String sql = "SELECT COUNT(*) FROM issuances WHERE booking_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка перевірки видачі: " + e.getMessage(), e);
        }
    }

    public void addIssuance(Issuance issuance) {
        String bookingSql =
                "SELECT status, publication_id FROM bookings WHERE id = ? FOR UPDATE";

        String publicationSql =
                "SELECT available_copies FROM publications WHERE id = ? FOR UPDATE";

        String duplicateSql =
                "SELECT COUNT(*) FROM issuances WHERE booking_id = ?";

        String updateCopiesSql =
                "UPDATE publications SET available_copies = available_copies - 1 " +
                        "WHERE id = ? AND available_copies > 0";

        String insertSql =
                "INSERT INTO issuances (booking_id, issue_date, return_date) " +
                        "VALUES (?, ?, NULL)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int publicationId;

                try (PreparedStatement statement =
                             connection.prepareStatement(bookingSql)) {

                    statement.setInt(1, issuance.getBookingId());

                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Бронювання не знайдено");
                        }

                        String status = rs.getString("status");

                        if (!"ГОТОВЕ_ДО_ВИДАЧІ".equals(status)) {
                            throw new IllegalArgumentException(
                                    "Книга може бути видана лише для бронювання зі статусом ГОТОВЕ_ДО_ВИДАЧІ");
                        }

                        publicationId = rs.getInt("publication_id");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(duplicateSql)) {

                    statement.setInt(1, issuance.getBookingId());

                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new IllegalArgumentException(
                                    "Для цього бронювання видачу вже створено");
                        }
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(publicationSql)) {

                    statement.setInt(1, publicationId);

                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Видання не знайдено");
                        }

                        if (rs.getInt("available_copies") <= 0) {
                            throw new IllegalArgumentException(
                                    "Немає доступних примірників");
                        }
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateCopiesSql)) {

                    statement.setInt(1, publicationId);

                    if (statement.executeUpdate() == 0) {
                        throw new IllegalArgumentException(
                                "Не вдалося оновити кількість примірників");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(insertSql)) {

                    statement.setInt(1, issuance.getBookingId());
                    statement.setTimestamp(
                            2,
                            new Timestamp(issuance.getIssueDate().getTime())
                    );

                    statement.executeUpdate();
                }

                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Помилка додавання видачі: " + e.getMessage(), e
            );
        }
    }

    public List<Issuance> getAllIssuances() {
        List<Issuance> issuances = new ArrayList<>();
        String sql = "SELECT * FROM issuances";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Issuance issuance = new Issuance();

                issuance.setId(rs.getInt("id"));
                issuance.setBookingId(rs.getInt("booking_id"));
                issuance.setIssueDate(rs.getTimestamp("issue_date"));
                issuance.setReturnDate(rs.getDate("return_date"));

                issuances.add(issuance);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка отримання видач: " + e.getMessage(), e);
        }

        return issuances;
    }

    public boolean returnIssuance(int issuanceId) {
        String selectSql =
                "SELECT i.booking_id, i.return_date, b.publication_id " +
                        "FROM issuances i " +
                        "JOIN bookings b ON b.id = i.booking_id " +
                        "WHERE i.id = ? FOR UPDATE";

        String returnSql =
                "UPDATE issuances SET return_date = CURRENT_DATE " +
                        "WHERE id = ? AND return_date IS NULL";

        String bookingSql =
                "UPDATE bookings SET status = 'ЗАВЕРШЕНО' WHERE id = ?";

        String publicationSql =
                "UPDATE publications " +
                        "SET available_copies = LEAST(total_copies, available_copies + 1) " +
                        "WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int bookingId;
                int publicationId;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {

                    statement.setInt(1, issuanceId);

                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next() || rs.getDate("return_date") != null) {
                            connection.rollback();
                            return false;
                        }

                        bookingId = rs.getInt("booking_id");
                        publicationId = rs.getInt("publication_id");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(returnSql)) {

                    statement.setInt(1, issuanceId);

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(bookingSql)) {

                    statement.setInt(1, bookingId);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException(
                                "Не вдалося завершити бронювання");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(publicationSql)) {

                    statement.setInt(1, publicationId);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException(
                                "Не вдалося оновити кількість примірників");
                    }
                }

                connection.commit();
                return true;

            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Помилка повернення книги: " + e.getMessage(), e
            );
        }
    }
}