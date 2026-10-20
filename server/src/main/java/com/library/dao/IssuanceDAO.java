package com.library.dao;

import com.library.model.Issuance;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IssuanceDAO {

    // Додати новий запис про видачу видання
    public void addIssuance(Issuance issuance) {
        String query = "INSERT INTO issuances (reader_id, publication_id, issue_date, due_date, return_date, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, issuance.getReaderId());
            statement.setInt(2, issuance.getPublicationId());
            statement.setDate(3, new java.sql.Date(issuance.getIssueDate().getTime()));
            statement.setDate(4, new java.sql.Date(issuance.getDueDate().getTime()));

            if (issuance.getReturnDate() != null) {
                statement.setDate(5, new java.sql.Date(issuance.getReturnDate().getTime()));
            } else {
                statement.setNull(5, Types.DATE);
            }

            statement.setString(6, issuance.getStatus());

            statement.executeUpdate();
            System.out.println("Запис про видачу успішно додано!");

        } catch (SQLException e) {
            System.err.println("Помилка при додаванні видачі: " + e.getMessage());
        }
    }

    // Отримати список усіх видач
    public List<Issuance> getAllIssuances() {
        List<Issuance> issuances = new ArrayList<>();
        String query = "SELECT * FROM issuances";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Issuance issuance = new Issuance(
                        resultSet.getInt("id"),
                        resultSet.getInt("reader_id"),
                        resultSet.getInt("publication_id"),
                        resultSet.getDate("issue_date"),
                        resultSet.getDate("due_date"),
                        resultSet.getDate("return_date"),
                        resultSet.getString("status")
                );
                issuances.add(issuance);
            }

        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку видач: " + e.getMessage());
        }

        return issuances;
    }
}