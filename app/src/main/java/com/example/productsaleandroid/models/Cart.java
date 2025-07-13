package com.example.productsaleandroid.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Cart {
    public int cartID;
    public double totalPrice;
    public String status;
    @SerializedName("cartItems")
    public List<CartItem> items;
}
