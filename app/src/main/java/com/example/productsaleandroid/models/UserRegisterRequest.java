package com.example.productsaleandroid.models;

public class UserRegisterRequest {
    public String username, password, email, phoneNumber, address;

    public UserRegisterRequest(String username, String password, String email, String phone, String address) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phone;
        this.address = address;
    }
}

