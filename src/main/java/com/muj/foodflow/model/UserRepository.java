package com.muj.foodflow.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.muj.foodflow.model.User;

public class UserRepository {

    public void saveUser(User user) {

        String sql = "INSERT INTO users " +
                     "(user_id, name, email, phone, password, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, user.getUserId());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getPassword());
            statement.setString(6, user.getRole());

            statement.executeUpdate();

            System.out.println("User saved to database successfully!");

        } catch (SQLException e) {

            System.out.println("Failed to save user.");
            e.printStackTrace();
        }
    }

    public void findUser(int userId) {

        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {

                System.out.println();
                System.out.println("===== USER FROM DATABASE =====");

                System.out.println("User ID: " + result.getInt("user_id"));
                System.out.println("Name: " + result.getString("name"));
                System.out.println("Email: " + result.getString("email"));
                System.out.println("Phone: " + result.getString("phone"));
                System.out.println("Role: " + result.getString("role"));

            } else {

                System.out.println("User not found.");
            }

        } catch (SQLException e) {

            System.out.println("Failed to find user.");
            e.printStackTrace();
        }
    }
}