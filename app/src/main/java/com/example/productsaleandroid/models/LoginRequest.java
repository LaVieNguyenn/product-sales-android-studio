package com.example.productsaleandroid.models;
import com.google.gson.annotations.SerializedName;
public class LoginRequest {
    @SerializedName("email")
    private String Email;
    @SerializedName("password")
    private String Password;

    public LoginRequest(String email, String password) {
        this.Email = email;
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
