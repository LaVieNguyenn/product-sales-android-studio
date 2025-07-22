package com.example.productsaleandroid.models;

import java.util.List;

public class WishlistListResponse {
    private boolean success;
    private String message;
    private List<WishlistItem> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<WishlistItem> getData() {
        return data;
    }

    public void setData(List<WishlistItem> data) {
        this.data = data;
    }
}
