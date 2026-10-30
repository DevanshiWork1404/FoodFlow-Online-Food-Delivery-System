package com.muj.foodflow.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressRepository {

    public int saveAddress(int userId,
                           String address,
                           String city,
                           String pincode) {

        String idSql =
                "SELECT COALESCE(MAX(address_id), 0) + 1 AS next_id " +
                "FROM addresses";

        String insertSql =
                "INSERT INTO addresses " +
                "(address_id, user_id, address, city, pincode) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement idStatement =
                     connection.prepareStatement(idSql);
             ResultSet result = idStatement.executeQuery()) {

            int addressId = 1;

            if (result.next()) {
                addressId = result.getInt("next_id");
            }

            try (PreparedStatement statement =
                    connection.prepareStatement(insertSql)) {

                statement.setInt(1, addressId);
                statement.setInt(2, userId);
                statement.setString(3, address);
                statement.setString(4, city);
                statement.setString(5, pincode);

                statement.executeUpdate();

                System.out.println("Address saved successfully!");

                return addressId;
            }

        } catch (SQLException e) {

            System.out.println("Failed to save address.");
            e.printStackTrace();
        }

        return -1;
    }
}