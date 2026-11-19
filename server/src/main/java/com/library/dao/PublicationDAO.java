
package com.library.dao;

import com.library.model.Publication;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationDAO {

    // Додати видання
    public void addPublication(Publication publication) {
        String query = "INSERT INTO publications " +
                "(title, author, year, total_copies, available_copies) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, publication.getTitle());
            statement.setString(2, publication.getAuthor());
            statement.setInt(3, publication.getYearPublished());
            statement.setInt(4, publication.getTotalCopies());
            statement.setInt(5, publication.getAvailableCopies());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Помилка додавання видання: " + e.getMessage(), e);
        }
    }

    // Отримати всі видання
    public List<Publication> getAllPublications() {
        List<Publication> publications = new ArrayList<>();
        String query = "SELECT * FROM publications";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Publication publication = new Publication(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getInt("year"),
                        resultSet.getInt("total_copies"),
                        resultSet.getInt("available_copies")
                );

                publications.add(publication);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка отримання видань: " + e.getMessage(), e);
        }

        return publications;
    }

    // Редагувати видання
    public void updatePublication(Publication publication) {
        String query = "UPDATE publications SET title=?, author=?, " +
                "year=?, total_copies=?, available_copies=? WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, publication.getTitle());
            statement.setString(2, publication.getAuthor());
            statement.setInt(3, publication.getYearPublished());
            statement.setInt(4, publication.getTotalCopies());
            statement.setInt(5, publication.getAvailableCopies());
            statement.setInt(6, publication.getId());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new IllegalArgumentException("Видання з таким ID не знайдено");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка редагування видання: " + e.getMessage(), e);
        }
    }

    // Видалити видання
    public void deletePublication(int id) {
        String query = "DELETE FROM publications WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new IllegalArgumentException("Видання з таким ID не знайдено");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка видалення видання: " + e.getMessage(), e);
        }
    }
}
