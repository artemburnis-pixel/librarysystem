package com.library.dao;

import com.library.model.Reader;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAO {

    public void addReader(Reader reader) {
        String query = "INSERT INTO readers (full_name, email, phone, address, registration_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, reader.getFullName());
            statement.setString(2, reader.getEmail());
            statement.setString(3, reader.getPhoneNumber());
            statement.setString(4, reader.getAddress());
            statement.setDate(5, new java.sql.Date(reader.getRegistrationDate().getTime()));

            statement.executeUpdate();
            System.out.println("Читача успішно додано до бази даних!");

        } catch (SQLException e) {
            System.err.println("Помилка при додаванні читача: " + e.getMessage());
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
            System.err.println("Помилка при отриманні списку читачів: " + e.getMessage());
        }

        return readers;
    }
}