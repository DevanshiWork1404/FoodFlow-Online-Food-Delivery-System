package com.muj.foodflow.model;

public class Customer extends User {

    public Customer() {
        super();
    }
    public Customer(int userId, String name, String email,
                    String phone, String password) {

        super(userId, name, email, phone, password, "Customer");
    }

    public void placeOrder() {
        System.out.println(getName() + " is placing an order.");
    }

    public void viewOrders() {
        System.out.println(getName() + " is viewing their orders.");
    }

    @Override
    public void displayUserDetails() {
        super.displayUserDetails();
    }
}