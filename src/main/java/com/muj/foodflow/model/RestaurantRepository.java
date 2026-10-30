package com.muj.foodflow.model;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import com.muj.foodflow.model.RestaurantOwner;

public class RestaurantRepository{

    public void saveRestaurant(int restaurantId, RestaurantOwner owner) {

        String sql = "INSERT INTO restaurants " +
                     "(restaurant_id, restaurant_name, restaurant_address, owner_id) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, restaurantId);
            statement.setString(2, owner.getRestaurantName());
            statement.setString(3, owner.getRestaurantAddress());
            statement.setInt(4, owner.getUserId());

            statement.executeUpdate();

            System.out.println("Restaurant saved to database successfully!");

        } catch (SQLException e) {

            System.out.println("Failed to save restaurant.");
            e.printStackTrace();
        }
    }
    public int selectRestaurant(Scanner scanner) {

    String sql = "SELECT restaurant_id, restaurant_name " +
                 "FROM restaurants ORDER BY restaurant_id";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet result = statement.executeQuery()) {

        System.out.println();
        System.out.println("===== RESTAURANTS =====");

        int count = 0;

        while (result.next()) {

            count++;

            System.out.println(
                count + ". " +
                result.getString("restaurant_name")
            );
        }

        if (count == 0) {
            System.out.println("No restaurants available.");
            return -1;
        }

        System.out.print("Select restaurant: ");
        int choice = scanner.nextInt();

        if (choice < 1 || choice > count) {
            System.out.println("Invalid restaurant selection.");
            return -1;
        }

        try (PreparedStatement secondStatement =
                connection.prepareStatement(
                    "SELECT restaurant_id FROM restaurants " +
                    "ORDER BY restaurant_id OFFSET ? LIMIT 1")) {

            secondStatement.setInt(1, choice - 1);

            ResultSet selectedResult =
                    secondStatement.executeQuery();

            if (selectedResult.next()) {
                return selectedResult.getInt("restaurant_id");
            }
        }

    } catch (SQLException e) {

        System.out.println("Failed to load restaurants.");
        e.printStackTrace();
    }

    return -1;
}
public String getRestaurantName(int restaurantId) {

    String sql = "SELECT restaurant_name FROM restaurants WHERE restaurant_id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, restaurantId);

        ResultSet result = statement.executeQuery();

        if (result.next()) {
            return result.getString("restaurant_name");
        }

    } catch (SQLException e) {

        System.out.println("Failed to get restaurant name.");
        e.printStackTrace();
    }

    return "Unknown Restaurant";
}
}