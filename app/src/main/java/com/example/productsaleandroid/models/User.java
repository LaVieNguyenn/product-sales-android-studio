package com.example.productsaleandroid.models;

public class User {
    private String username;
    private String email;
    private String phoneNumber;
    private String address;

    // Constructor đầy đủ (dùng cho update profile)
    public User(String username, String email, String phoneNumber, String address) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // ... các constructor khác, getter/setter nếu cần

    // Getter & Setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}

