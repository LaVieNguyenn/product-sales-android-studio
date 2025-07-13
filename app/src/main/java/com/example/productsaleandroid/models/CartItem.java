package com.example.productsaleandroid.models;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("cartItemId")
    public int cartItemID;
    public int productID;
    public int quantity;
    public double price;
    public Product product;
    public boolean isSelected = false;
}
