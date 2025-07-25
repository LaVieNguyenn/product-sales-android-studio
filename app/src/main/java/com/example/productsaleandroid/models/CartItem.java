package com.example.productsaleandroid.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CartItem implements Serializable {
    public CartItem(int productID, String productName, double price, int quantity, String imageUrl) {
        this.productID = productID;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    @SerializedName("cartItemId")
    public int cartItemID;
    public int productID;
    public int quantity;
    public double price;
    public Product product;
    public boolean isSelected = false;
    public String productName;
    public String description;
    public String imageUrl;

}
