package com.library.dao;

import com.library.model.Reader;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAO {

    public void addReader(Reader reader) {
        String query = "INSERT INTO readers " +
                "(full_name, email, phone, address, registration_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, reader.getFullName());
            statement.setString(2, reader.getEmail());
            statement.setString(3, reader.getPhoneNumber());
            statement.setString(4, reader.getAddress());

            if (reader.getRegistrationDate() != null) {
                statement.setDate(5,
                        new java.sql.Date(reader.getRegistrationDate().getTime()));
            } else {
                statement.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
            }

            statement.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException(
                    "Читач із таким Email уже існує");
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка при додаванні читача: " + e.getMessage(), e);
        }
    }

    public List<Reader> getAllReaders() {
        List<Reader> readers = new ArrayList<>();
        String query = "SELECT * FROM readers";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Reader reader = new Reader(
                        resultSet.getInt("id"),
                        resultSet.getString("full_name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("address"),
                        resultSet.getDate("registration_date")
                );

                readers.add(reader);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка при отриманні читачів: " + e.getMessage(), e);
        }

        return readers;
    }

    public boolean updateReader(Reader reader) {
        String query = "UPDATE readers SET full_name = ?, email = ?, " +
                "phone = ?, address = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, reader.getFullName());
            statement.setString(2, reader.getEmail());
            statement.setString(3, reader.getPhoneNumber());
            statement.setString(4, reader.getAddress());
            statement.setInt(5, reader.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException(
                    "Читач із таким Email уже існує");
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Помилка при редагуванні читача: " + e.getMessage(), e);
        }
    }

    public boolean deleteReader(int id) {
        String query = "DELETE FROM readers WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Не вдалося видалити читача. Можливо, він має пов'язані бронювання. " +
                            e.getMessage(), e);
        }
    }
}