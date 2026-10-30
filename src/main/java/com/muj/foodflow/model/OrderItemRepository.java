package com.muj.foodflow.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderItemRepository {

    public int saveOrderItem(int orderId,
                             int foodId,
                             int quantity) {

        String idSql =
                "SELECT COALESCE(MAX(order_item_id), 0) + 1 AS next_id " +
                "FROM order_items";

        String insertSql =
                "INSERT INTO order_items " +
                "(order_item_id, order_id, food_id, quantity) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement idStatement =
                     connection.prepareStatement(idSql);
             ResultSet result = idStatement.executeQuery()) {

            int orderItemId = 1;

            if (result.next()) {
                orderItemId = result.getInt("next_id");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(insertSql)) {

                statement.setInt(1, orderItemId);
                statement.setInt(2, orderId);
                statement.setInt(3, foodId);
                statement.setInt(4, quantity);

                statement.executeUpdate();

                System.out.println("Order item saved successfully!");

                return orderItemId;
            }

        } catch (SQLException e) {

            System.out.println("Failed to save order item.");
            e.printStackTrace();
        }

        return -1;
    }
}
