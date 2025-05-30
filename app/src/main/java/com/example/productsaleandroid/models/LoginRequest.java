package com.example.productsaleandroid.models;

public class LoginRequest {
    private String Email;
    private String Password;

    public LoginRequest(String username, String password) {
        this.Email = username;
        this.Password = password;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        this.Password = password;
    }
}
