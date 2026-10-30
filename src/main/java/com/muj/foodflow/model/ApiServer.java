package com.muj.foodflow.model;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiServer {

    // Runs a SELECT and returns the rows as a list of maps (becomes JSON)
    static List<Map<String, Object>> rows(String sql, Object... params) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) s.setObject(i + 1, params[i]);
            ResultSet r = s.executeQuery();
            ResultSetMetaData m = r.getMetaData();
            List<Map<String, Object>> out = new ArrayList<>();
            while (r.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= m.getColumnCount(); i++) row.put(m.getColumnLabel(i), r.getObject(i));
                out.add(row);
            }
            return out;
        }
    }

    // Runs an INSERT / UPDATE / DELETE
    static void exec(String sql, Object... params) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) s.setObject(i + 1, params[i]);
            s.executeUpdate();
        }
    }

    // True when the user id exists and has the given role
    static boolean hasRole(int uid, String role) throws SQLException {
        return !rows("SELECT 1 FROM users WHERE user_id = ? AND role = ?", uid, role).isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        Javalin app = Javalin.create(config ->
                config.staticFiles.add("/public", Location.CLASSPATH)
        ).start(7070);

        // POST /api/login  {email, password}
        app.post("/api/login", ctx -> {
            Map<String, Object> b = ctx.bodyAsClass(Map.class);
            List<Map<String, Object>> found = rows(
                    "SELECT user_id, name, role FROM users WHERE email = ? AND password = ?",
                    b.get("email"), b.get("password"));
            if (found.isEmpty()) ctx.status(401).json(Map.of("error", "Wrong email or password."));
            else ctx.json(found.get(0));
        });

        // POST /api/signup  {name, email, phone, password}  -> creates a new Customer
        app.post("/api/signup", ctx -> {
            Map<String, Object> b = ctx.bodyAsClass(Map.class);
            String name = String.valueOf(b.getOrDefault("name", "")).trim();
            String email = String.valueOf(b.getOrDefault("email", "")).trim();
            String phone = String.valueOf(b.getOrDefault("phone", "")).trim();
            String password = String.valueOf(b.getOrDefault("password", ""));
            String role = String.valueOf(b.getOrDefault("role", "Customer"));
            String restName = String.valueOf(b.getOrDefault("restaurantName", "")).trim();
            String restAddr = String.valueOf(b.getOrDefault("restaurantAddress", "")).trim();
            if (!role.equals("Customer") && !role.equals("Restaurant Owner")) {
                ctx.status(400).json(Map.of("error", "Choose Customer or Restaurant Owner."));
                return;
            }
            if (role.equals("Restaurant Owner") && (restName.isEmpty() || restAddr.isEmpty())) {
                ctx.status(400).json(Map.of("error", "Please enter your restaurant name and address."));
                return;
            }

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                ctx.status(400).json(Map.of("error", "Please fill in every field."));
                return;
            }
            if (name.length() > 100 || email.length() > 100 || phone.length() > 15) {
                ctx.status(400).json(Map.of("error", "Name, email or phone number is too long."));
                return;
            }
            if (!rows("SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)", email).isEmpty()) {
                ctx.status(409).json(Map.of("error", "An account with this email already exists. Try logging in."));
                return;
            }

            int newId = ((Number) rows("SELECT COALESCE(MAX(user_id), 0) + 1 AS next_id FROM users")
                    .get(0).get("next_id")).intValue();

            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement s = c.prepareStatement(
                         "INSERT INTO users (user_id, name, email, phone, password, role) VALUES (?, ?, ?, ?, ?, ?)")) {
                s.setInt(1, newId);
                s.setString(2, name);
                s.setString(3, email);
                s.setString(4, phone);
                s.setString(5, password);
                s.setString(6, role);
                s.executeUpdate();
            }

            if (role.equals("Restaurant Owner")) {
                int rid = ((Number) rows("SELECT COALESCE(MAX(restaurant_id), 0) + 1 AS n FROM restaurants")
                        .get(0).get("n")).intValue();
                exec("INSERT INTO restaurants (restaurant_id, restaurant_name, restaurant_address, owner_id) " +
                     "VALUES (?, ?, ?, ?)", rid, restName, restAddr, newId);
            }
            ctx.status(201).json(Map.of("user_id", newId, "name", name, "role", role));
        });

        // ---------------- RESTAURANT OWNER ----------------
        // GET /api/owner/{uid}/menu -> {restaurant, items}
        app.get("/api/owner/{uid}/menu", ctx -> {
            int uid = Integer.parseInt(ctx.pathParam("uid"));
            if (!hasRole(uid, "Restaurant Owner")) { ctx.status(403).json(Map.of("error", "Owners only.")); return; }
            List<Map<String, Object>> r = rows("SELECT restaurant_id, restaurant_name FROM restaurants WHERE owner_id = ?", uid);
            if (r.isEmpty()) { ctx.json(Map.of("restaurant", "", "items", List.of())); return; }
            ctx.json(Map.of("restaurant", r.get(0).get("restaurant_name"), "items", rows(
                    "SELECT food_id, food_name, description, price, category FROM food_items " +
                    "WHERE restaurant_id = ? ORDER BY food_id", r.get(0).get("restaurant_id"))));
        });

        // POST /api/owner/{uid}/food  {name, description, price, category}
        app.post("/api/owner/{uid}/food", ctx -> {
            int uid = Integer.parseInt(ctx.pathParam("uid"));
            if (!hasRole(uid, "Restaurant Owner")) { ctx.status(403).json(Map.of("error", "Owners only.")); return; }
            Map<String, Object> b = ctx.bodyAsClass(Map.class);
            List<Map<String, Object>> r = rows("SELECT restaurant_id FROM restaurants WHERE owner_id = ?", uid);
            if (r.isEmpty()) { ctx.status(400).json(Map.of("error", "You have no restaurant yet.")); return; }
            int id = ((Number) rows("SELECT COALESCE(MAX(food_id), 0) + 1 AS n FROM food_items")
                    .get(0).get("n")).intValue();
            exec("INSERT INTO food_items (food_id, restaurant_id, food_name, description, price, category) " +
                 "VALUES (?, ?, ?, ?, ?, ?)", id, r.get(0).get("restaurant_id"), b.get("name"),
                 b.get("description"), new java.math.BigDecimal(String.valueOf(b.get("price"))), b.get("category"));
            ctx.status(201).json(Map.of("food_id", id));
        });

        // DELETE /api/owner/{uid}/food/{fid}
        app.delete("/api/owner/{uid}/food/{fid}", ctx -> {
            int uid = Integer.parseInt(ctx.pathParam("uid"));
            if (!hasRole(uid, "Restaurant Owner")) { ctx.status(403).json(Map.of("error", "Owners only.")); return; }
            try {
                exec("DELETE FROM food_items WHERE food_id = ? AND restaurant_id IN " +
                     "(SELECT restaurant_id FROM restaurants WHERE owner_id = ?)",
                     Integer.parseInt(ctx.pathParam("fid")), uid);
                ctx.json(Map.of("ok", true));
            } catch (SQLException e) {
                ctx.status(409).json(Map.of("error", "This item is part of past orders and cannot be removed."));
            }
        });

        // GET /api/owner/{uid}/orders  -> orders received by this owner's restaurant
        app.get("/api/owner/{uid}/orders", ctx -> {
            int uid = Integer.parseInt(ctx.pathParam("uid"));
            if (!hasRole(uid, "Restaurant Owner")) { ctx.status(403).json(Map.of("error", "Owners only.")); return; }
            ctx.json(rows(
                "SELECT o.order_id, u.name AS customer, o.order_date, o.total_amount, o.status, " +
                "o.payment_method, o.payment_status, " +
                "(SELECT string_agg(f.food_name || ' x ' || oi.quantity, ', ') FROM order_items oi " +
                "  JOIN food_items f ON f.food_id = oi.food_id WHERE oi.order_id = o.order_id) AS items " +
                "FROM orders o JOIN restaurants r ON r.restaurant_id = o.restaurant_id " +
                "JOIN users u ON u.user_id = o.customer_id WHERE r.owner_id = ? ORDER BY o.order_id DESC", uid));
        });

        // ---------------- ADMIN ----------------
        // GET /api/admin/{uid}/summary | users | orders
        app.get("/api/admin/{uid}/{what}", ctx -> {
            int uid = Integer.parseInt(ctx.pathParam("uid"));
            if (!hasRole(uid, "Admin")) { ctx.status(403).json(Map.of("error", "Admins only.")); return; }
            switch (ctx.pathParam("what")) {
                case "summary" -> ctx.json(rows(
                    "SELECT (SELECT COUNT(*) FROM users) AS users, (SELECT COUNT(*) FROM restaurants) AS restaurants, " +
                    "(SELECT COUNT(*) FROM orders) AS orders, (SELECT COALESCE(SUM(total_amount), 0) FROM orders) AS revenue").get(0));
                case "users" -> ctx.json(rows("SELECT user_id, name, email, phone, role FROM users ORDER BY user_id"));
                case "orders" -> ctx.json(rows(
                    "SELECT o.order_id, u.name AS customer, r.restaurant_name, o.order_date, o.total_amount, o.status, " +
                    "o.payment_status FROM orders o JOIN users u ON u.user_id = o.customer_id " +
                    "JOIN restaurants r ON r.restaurant_id = o.restaurant_id ORDER BY o.order_id DESC"));
                default -> ctx.status(404).json(Map.of("error", "Unknown report."));
            }
        });

        // GET /api/restaurants
        app.get("/api/restaurants", ctx -> ctx.json(rows(
                "SELECT restaurant_id, restaurant_name, restaurant_address FROM restaurants ORDER BY restaurant_id")));

        // GET /api/restaurants/{id}/menu
        app.get("/api/restaurants/{id}/menu", ctx -> ctx.json(rows(
                "SELECT food_id, food_name, description, price, category FROM food_items " +
                "WHERE restaurant_id = ? ORDER BY food_id",
                Integer.parseInt(ctx.pathParam("id")))));

        // GET /api/users/{id}/orders
        app.get("/api/users/{id}/orders", ctx -> ctx.json(rows(
                "SELECT o.order_id, r.restaurant_name, o.order_date, o.total_amount, o.status, " +
                "o.payment_method, o.payment_status FROM orders o " +
                "JOIN restaurants r ON r.restaurant_id = o.restaurant_id " +
                "WHERE o.customer_id = ? ORDER BY o.order_id DESC",
                Integer.parseInt(ctx.pathParam("id")))));

        // POST /api/orders  {userId, restaurantId, address, city, pincode, paymentMethod, items:[{foodId, quantity}]}
        app.post("/api/orders", ctx -> {
            Map<String, Object> b = ctx.bodyAsClass(Map.class);
            int userId = ((Number) b.get("userId")).intValue();
            int restaurantId = ((Number) b.get("restaurantId")).intValue();
            List<Map<String, Object>> items = (List<Map<String, Object>>) b.get("items");

            // Total is calculated here from database prices, never trusted from the browser
            FoodItemRepository foods = new FoodItemRepository();
            double total = 0;
            for (Map<String, Object> it : items) {
                total += foods.getFoodPrice(((Number) it.get("foodId")).intValue())
                        * ((Number) it.get("quantity")).intValue();
            }

            int addressId = new AddressRepository().saveAddress(
                    userId, (String) b.get("address"), (String) b.get("city"), (String) b.get("pincode"));

            String method = (String) b.get("paymentMethod");
            String payStatus = method.equals("Cash on Delivery") ? "Pending" : "Paid";

            int orderId = new OrderRepository().saveOrder(
                    userId, restaurantId, addressId, total, "Placed", method, payStatus);

            if (addressId == -1 || orderId == -1) {
                ctx.status(500).json(Map.of("error", "Order could not be saved."));
                return;
            }

            OrderItemRepository orderItems = new OrderItemRepository();
            for (Map<String, Object> it : items) {
                orderItems.saveOrderItem(orderId,
                        ((Number) it.get("foodId")).intValue(),
                        ((Number) it.get("quantity")).intValue());
            }

            ctx.json(Map.of("orderId", orderId, "total", total, "paymentStatus", payStatus));
        });

        System.out.println("FoodFlow is running at http://localhost:7070");
    }
}