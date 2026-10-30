package com.muj.foodflow.model;

import java.util.ArrayList;
import java.util.Scanner;

import com.muj.foodflow.model.Admin;
import com.muj.foodflow.model.Customer;
import com.muj.foodflow.model.RestaurantOwner;
import com.muj.foodflow.model.User;

public class main {

    public static void main(String[] args) {

        DatabaseConnection.getConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.println("===== FOODFLOW =====");

        System.out.print("Enter User ID: ");
        int userId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        System.out.print("Enter Mobile Number: ");
        String phone = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        System.out.println();
        System.out.println("Select Role:");
        System.out.println("1. Customer");
        System.out.println("2. Restaurant Owner");
        System.out.println("3. Admin");

        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();

        User user;

        switch (choice) {

            case 1:

                user = new Customer(
                        userId,
                        name,
                        email,
                        phone,
                        password
                );

                break;

            case 2:

                scanner.nextLine();

                System.out.print("Enter Restaurant Name: ");
                String restaurantName = scanner.nextLine();

                System.out.print("Enter Restaurant Address: ");
                String restaurantAddress = scanner.nextLine();

                user = new RestaurantOwner(
                        userId,
                        name,
                        email,
                        phone,
                        password,
                        restaurantName,
                        restaurantAddress
                );

                break;

            case 3:

                user = new Admin(
                        userId,
                        name,
                        email,
                        phone,
                        password
                );

                break;

            default:

                System.out.println("Invalid role selected.");
                scanner.close();
                return;
        }

        // ================= SAVE USER =================

        UserRepository userRepository =
                new UserRepository();

        userRepository.saveUser(user);


        // =========================================================
        // ===================== CUSTOMER ==========================
        // =========================================================

        if (user instanceof Customer) {

            System.out.println();
            System.out.println("===== PLACE ORDER =====");

            RestaurantRepository restaurantRepository =
                    new RestaurantRepository();

            int restaurantId =
                    restaurantRepository.selectRestaurant(scanner);

            if (restaurantId == -1) {

                System.out.println("Order cancelled.");
                scanner.close();
                return;
            }

            String selectedRestaurantName =
                    restaurantRepository.getRestaurantName(restaurantId);


            // ================= CART =================

            FoodItemRepository foodItemRepository =
                    new FoodItemRepository();

            ArrayList<Integer> foodIds =
                    new ArrayList<>();

            ArrayList<Integer> quantities =
                    new ArrayList<>();

            ArrayList<String> foodNames =
                    new ArrayList<>();

            ArrayList<Double> foodPrices =
                    new ArrayList<>();

            double totalAmount = 0;

            boolean addMore = true;

            while (addMore) {

                System.out.println();
                System.out.println("===== MENU =====");

                int foodId =
                        foodItemRepository.selectFood(
                                restaurantId,
                                scanner
                        );

                if (foodId == -1) {

                    System.out.println("Food selection cancelled.");
                    scanner.close();
                    return;
                }

                String foodName =
                        foodItemRepository.getFoodName(foodId);

                double price =
                        foodItemRepository.getFoodPrice(foodId);


                System.out.print("Enter Quantity: ");
                int quantity = scanner.nextInt();

                if (quantity <= 0) {

                    System.out.println(
                            "Invalid quantity. Please enter a quantity greater than 0."
                    );

                    continue;
                }


                // Add item to cart

                foodIds.add(foodId);
                quantities.add(quantity);
                foodNames.add(foodName);
                foodPrices.add(price);

                double itemTotal =
                        price * quantity;

                totalAmount =
                        totalAmount + itemTotal;


                System.out.println();
                System.out.println(
                        foodName +
                        " x " +
                        quantity +
                        " added to cart!"
                );

                System.out.println(
                        "Item Total: ₹" + itemTotal
                );


                // ================= ADD MORE =================

                System.out.println();
                System.out.println("Add another item?");
                System.out.println("1. Yes");
                System.out.println("2. No");

                System.out.print("Enter your choice: ");
                int moreChoice = scanner.nextInt();

                if (moreChoice == 2) {

                    addMore = false;

                } else if (moreChoice != 1) {

                    System.out.println(
                            "Invalid choice. Cart checkout will continue."
                    );

                    addMore = false;
                }
            }


            if (foodIds.isEmpty()) {

                System.out.println("Cart is empty.");
                scanner.close();
                return;
            }


            // ================= ADDRESS =================

            scanner.nextLine();

            System.out.println();
            System.out.println("===== DELIVERY ADDRESS =====");

            System.out.print("Enter Address: ");
            String address = scanner.nextLine();

            System.out.print("Enter City: ");
            String city = scanner.nextLine();

            System.out.print("Enter Pincode: ");
            String pincode = scanner.nextLine();


            AddressRepository addressRepository =
                    new AddressRepository();

            int addressId =
                    addressRepository.saveAddress(
                            user.getUserId(),
                            address,
                            city,
                            pincode
                    );

            if (addressId == -1) {

                System.out.println(
                        "Unable to save address."
                );

                scanner.close();
                return;
            }


            // ================= FINAL RECEIPT =================

            System.out.println();
            System.out.println("========== ORDER RECEIPT ==========");
            System.out.println(
                    "Restaurant      : " +
                    selectedRestaurantName
            );

            System.out.println("-----------------------------------");

            for (int i = 0; i < foodIds.size(); i++) {

                double itemTotal =
                        foodPrices.get(i) *
                        quantities.get(i);

                System.out.println(
                        foodNames.get(i) +
                        " x " +
                        quantities.get(i) +
                        " = ₹" +
                        itemTotal
                );
            }

            System.out.println("-----------------------------------");

            System.out.println(
                    "Delivery Address: " +
                    address
            );

            System.out.println(
                    "City            : " +
                    city
            );

            System.out.println(
                    "Pincode         : " +
                    pincode
            );

            System.out.println("-----------------------------------");

            System.out.println(
                    "Total Amount    : ₹" +
                    totalAmount
            );

            System.out.println("===================================");


            // ================= PAYMENT =================

            System.out.println();
            System.out.println("===== PAYMENT METHOD =====");

            System.out.println("1. UPI");
            System.out.println("2. Card");
            System.out.println("3. Cash on Delivery");

            System.out.print("Choose payment method: ");
            int paymentChoice = scanner.nextInt();

            String paymentMethod;
            String paymentStatus;


            switch (paymentChoice) {

                case 1:

                    paymentMethod = "UPI";
                    paymentStatus = "Paid";

                    System.out.println();
                    System.out.println("Payment processing...");
                    System.out.println("Payment successful!");

                    break;


                case 2:

                    paymentMethod = "Card";
                    paymentStatus = "Paid";

                    System.out.println();
                    System.out.println("Payment processing...");
                    System.out.println("Payment successful!");

                    break;


                case 3:

                    paymentMethod = "Cash on Delivery";
                    paymentStatus = "Pending";

                    System.out.println();
                    System.out.println(
                            "Cash on Delivery selected."
                    );

                    break;


                default:

                    System.out.println(
                            "Invalid payment method."
                    );

                    scanner.close();
                    return;
            }


            // ================= SAVE ORDER =================

            OrderRepository orderRepository =
                    new OrderRepository();

            int orderId =
                    orderRepository.saveOrder(
                            user.getUserId(),
                            restaurantId,
                            addressId,
                            totalAmount,
                            "Placed",
                            paymentMethod,
                            paymentStatus
                    );

            if (orderId == -1) {

                System.out.println(
                        "Order could not be placed."
                );

                scanner.close();
                return;
            }


            // ================= SAVE ORDER ITEMS =================

            OrderItemRepository orderItemRepository =
                    new OrderItemRepository();

            boolean itemsSaved = true;

            for (int i = 0; i < foodIds.size(); i++) {

                int orderItemId =
                        orderItemRepository.saveOrderItem(
                                orderId,
                                foodIds.get(i),
                                quantities.get(i)
                        );

                if (orderItemId == -1) {

                    itemsSaved = false;
                    break;
                }
            }


            if (!itemsSaved) {

                System.out.println(
                        "Some order items could not be saved."
                );

                scanner.close();
                return;
            }


            // ================= ORDER CONFIRMATION =================

            System.out.println();
            System.out.println("===== ORDER CONFIRMED =====");

            System.out.println(
                    "Order Status    : Placed"
            );

            System.out.println(
                    "Payment Method  : " +
                    paymentMethod
            );

            System.out.println(
                    "Payment Status  : " +
                    paymentStatus
            );

            System.out.println(
                    "Total Amount    : ₹" +
                    totalAmount
            );

            System.out.println();

            System.out.println(
                    "Your order has been placed successfully!"
            );
        }


        // =========================================================
        // ================= RESTAURANT OWNER =====================
        // ========================================================
else if (user instanceof RestaurantOwner) {

    System.out.println();
    System.out.println("===== RESTAURANT OWNER PANEL =====");

    System.out.println("1. Add Food Item");
    System.out.println("2. Remove Food Item");
    System.out.println("3. View Orders");
    System.out.println("4. Exit");

    System.out.print("Enter your choice: ");
    int ownerChoice = scanner.nextInt();

    switch (ownerChoice) {

        case 1:
            System.out.println(
                    "Food item added successfully!"
            );
            break;

        case 2:
            System.out.println(
                    "Food item removed successfully!"
            );
            break;

        case 3:
            System.out.println(
                    "Orders displayed successfully!"
            );
            break;

        case 4:
            System.out.println(
                    "Exiting Restaurant Owner Panel."
            );
            break;

        default:
            System.out.println(
                    "Invalid owner option."
            );
    }
}

        // =========================================================
        // ======================== ADMIN ==========================
        // =========================================================

        else if (user instanceof Admin) {

            System.out.println();
            System.out.println("===== ADMIN PANEL =====");

            System.out.println("1. Manage Users");
            System.out.println("2. Manage Restaurants");
            System.out.println("3. View Reports");
            System.out.println("4. Exit");

            System.out.print("Enter your choice: ");
            int adminChoice = scanner.nextInt();

            switch (adminChoice) {

                case 1:

                    System.out.println(
                            "Users managed successfully!"
                    );

                    break;


                case 2:

                    System.out.println(
                            "Restaurants managed successfully!"
                    );

                    break;


                case 3:

                    System.out.println(
                            "Reports generated successfully!"
                    );

                    break;


                case 4:

                    System.out.println(
                            "Exiting Admin Panel."
                    );

                    break;


                default:

                    System.out.println(
                            "Invalid admin option."
                    );
            }
        }


        // ================= FINAL USER DETAILS =================

        System.out.println();
        System.out.println("===== USER CREATED =====");

        user.displayUserDetails();

        System.out.println();

        user.login();

        scanner.close();
    }
}