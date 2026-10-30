package com.muj.foodflow.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class FoodItemRepository {

    public void saveFoodItem(int foodId,
                             int restaurantId,
                             String foodName,
                             String description,
                             double price,
                             String category) {

        String sql = "INSERT INTO food_items " +
                     "(food_id, restaurant_id, food_name, description, price, category) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, foodId);
            statement.setInt(2, restaurantId);
            statement.setString(3, foodName);
            statement.setString(4, description);
            statement.setDouble(5, price);
            statement.setString(6, category);

            statement.executeUpdate();

            System.out.println("Food item saved to database successfully!");

        } catch (SQLException e) {

            System.out.println("Failed to save food item.");
            e.printStackTrace();
        }
    }
    public double getFoodPrice(int foodId) {

    String sql = "SELECT price FROM food_items WHERE food_id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, foodId);

        ResultSet result = statement.executeQuery();

        if (result.next()) {
            return result.getDouble("price");
        }

    } catch (SQLException e) {

        System.out.println("Failed to get food price.");
        e.printStackTrace();
    }

    return 0;
}
public String getFoodName(int foodId) {

    String sql = "SELECT food_name FROM food_items WHERE food_id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, foodId);

        ResultSet result = statement.executeQuery();

        if (result.next()) {
            return result.getString("food_name");
        }

    } catch (SQLException e) {

        System.out.println("Failed to get food name.");
        e.printStackTrace();
    }

    return "Unknown Food";
}
public int selectFood(int restaurantId, Scanner scanner) {

    String sql = "SELECT food_id, food_name, price " +
                 "FROM food_items " +
                 "WHERE restaurant_id = ? " +
                 "ORDER BY food_id";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, restaurantId);

        ResultSet result = statement.executeQuery();

        System.out.println();
        System.out.println("===== MENU =====");

        int count = 0;

        while (result.next()) {

            count++;

            System.out.println(
                count + ". " +
                result.getString("food_name") +
                " - ₹" +
                result.getDouble("price")
            );
        }

        if (count == 0) {
            System.out.println("No food items available.");
            return -1;
        }

        System.out.print("Select food: ");
        int choice = scanner.nextInt();

        if (choice < 1 || choice > count) {
            System.out.println("Invalid food selection.");
            return -1;
        }

        try (PreparedStatement secondStatement =
                connection.prepareStatement(
                    "SELECT food_id FROM food_items " +
                    "WHERE restaurant_id = ? " +
                    "ORDER BY food_id OFFSET ? LIMIT 1")) {

            secondStatement.setInt(1, restaurantId);
            secondStatement.setInt(2, choice - 1);

            ResultSet selectedResult =
                    secondStatement.executeQuery();

            if (selectedResult.next()) {
                return selectedResult.getInt("food_id");
            }
        }

    } catch (SQLException e) {

        System.out.println("Failed to load menu.");
        e.printStackTrace();
    }

    return -1;
}


}