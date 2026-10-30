package com.muj.foodflow.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderRepository {

    public int saveOrder(int customerId,
                         int restaurantId,
                         int addressId,
                         double totalAmount,
                         String status,
                         String paymentMethod,
                         String paymentStatus) {

        String idSql =
                "SELECT COALESCE(MAX(order_id), 0) + 1 AS next_id " +
                "FROM orders";

        String insertSql =
                "INSERT INTO orders " +
                "(order_id, customer_id, restaurant_id, address_id, " +
                "total_amount, status, payment_method, payment_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement idStatement =
                     connection.prepareStatement(idSql);
             ResultSet result = idStatement.executeQuery()) {

            int orderId = 1;

            if (result.next()) {
                orderId = result.getInt("next_id");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(insertSql)) {

                statement.setInt(1, orderId);
                statement.setInt(2, customerId);
                statement.setInt(3, restaurantId);
                statement.setInt(4, addressId);
                statement.setDouble(5, totalAmount);
                statement.setString(6, status);
                statement.setString(7, paymentMethod);
                statement.setString(8, paymentStatus);

                statement.executeUpdate();

                System.out.println("Order saved successfully!");

                return orderId;
            }

        } catch (SQLException e) {

            System.out.println("Failed to save order.");
            e.printStackTrace();
        }

        return -1;
    }
}