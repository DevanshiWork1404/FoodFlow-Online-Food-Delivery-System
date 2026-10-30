package com.muj.foodflow.model;

public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(int userId, String name, String email,
                 String phone, String password) {

        super(userId, name, email, phone, password, "Admin");
    }

    public void manageUsers() {
        System.out.println("Admin is managing users.");
    }

    public void manageRestaurants() {
        System.out.println("Admin is managing restaurants.");
    }

    public void viewReports() {
        System.out.println("Admin is viewing system reports.");
    }

    @Override
    public void displayUserDetails() {
        super.displayUserDetails();
    }
}