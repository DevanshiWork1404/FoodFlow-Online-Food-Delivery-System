package com.muj.foodflow.model;

public class RestaurantOwner extends User {

    private String restaurantName;
    private String restaurantAddress;

    public RestaurantOwner() {
        super();
    }

    public RestaurantOwner(int userId, String name, String email,
                           String phone, String password,
                           String restaurantName,
                           String restaurantAddress) {

        super(userId, name, email, phone, password, "Restaurant Owner");

        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public void addFoodItem() {
        System.out.println(restaurantName + " is adding a food item.");
    }

    public void removeFoodItem() {
        System.out.println(restaurantName + " is removing a food item.");
    }

    public void viewOrders() {
        System.out.println(restaurantName + " is viewing incoming orders.");
    }

    @Override
    public void displayUserDetails() {
        super.displayUserDetails();
        System.out.println("Restaurant Name: " + restaurantName);
        System.out.println("Restaurant Address: " + restaurantAddress);
    }
}
