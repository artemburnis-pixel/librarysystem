package com.library.dao;

import com.library.model.Publication;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationDAO {

    // Додати нове видання до бази даних
    public void addPublication(Publication publication) {
        String query = "INSERT INTO publications (title, author, year_published, genre, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, publication.getTitle());
            statement.setString(2, publication.getAuthor());
            statement.setInt(3, publication.getYearPublished());
            statement.setString(4, publication.getGenre());
            statement.setInt(5, publication.getTotalCopies());
            statement.setInt(6, publication.getAvailableCopies());

            statement.executeUpdate();
            System.out.println("Видання успішно додано!");

        } catch (SQLException e) {
            System.err.println("Помилка при додаванні видання: " + e.getMessage());
        }
    }

    // Отримати список усіх видань
    public List<Publication> getAllPublications() {
        List<Publication> publications = new ArrayList<>();
        String query = "SELECT * FROM publications";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Publication pub = new Publication(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getInt("year_published"),
                        resultSet.getString("genre"),
                        resultSet.getInt("total_copies"),
                        resultSet.getInt("available_copies")
                );
                publications.add(pub);
            }

        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку видань: " + e.getMessage());
        }

        return publications;
    }
}