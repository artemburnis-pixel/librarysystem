package com.library.dao;

import com.library.model.Booking;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public void addBooking(Booking booking) {
        String query = "INSERT INTO bookings (reader_id, publication_id, booking_date, expiry_date, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, booking.getReaderId());
            statement.setInt(2, booking.getPublicationId());
            statement.setDate(3, new java.sql.Date(booking.getBookingDate().getTime()));
            statement.setDate(4, new java.sql.Date(booking.getExpiryDate().getTime()));
            statement.setString(5, booking.getStatus());

            statement.executeUpdate();
            System.out.println("Бронювання успішно додано!");

        } catch (SQLException e) {
            System.err.println("Помилка при додаванні бронювання: " + e.getMessage());
        }
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM bookings";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Booking booking = new Booking(
                        resultSet.getInt("id"),
                        resultSet.getInt("reader_id"),
                        resultSet.getInt("publication_id"),
                        resultSet.getDate("booking_date"),
                        resultSet.getDate("expiry_date"),
                        resultSet.getString("status")
                );
                bookings.add(booking);
            }

        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку бронювань: " + e.getMessage());
        }

        return bookings;
    }
}