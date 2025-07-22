package com.example.productsaleandroid.models;

import java.util.List;

public class WishlistResponse {
    private boolean success;
    private String message;
    private WishlistItem data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public WishlistItem getData() { return data; }
    public void setData(WishlistItem data) { this.data = data; }
}
