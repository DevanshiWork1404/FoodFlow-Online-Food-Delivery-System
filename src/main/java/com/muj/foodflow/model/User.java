package com.muj.foodflow.model;

public class User {

    // Private attributes - Encapsulation
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role;

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(int userId, String name, String email,
                String phone, String password, String role) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Login method
    public void login() {
        System.out.println(name + " has logged in.");
    }

    // Logout method
    public void logout() {
        System.out.println(name + " has logged out.");
    }

    // Display user details
    public void displayUserDetails() {
        System.out.println("User ID: " + userId);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
        System.out.println("Role: " + role);
    }
}