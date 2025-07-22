package com.example.productsaleandroid.models;

import com.google.gson.annotations.SerializedName;

public class OrderCartItem {
    @SerializedName("cartItemId")
    public int cartItemId;

    @SerializedName("cartId")
    public int cartId;

    @SerializedName("productId")
    public int productId;

    @SerializedName("quantity")
    public int quantity;

    @SerializedName("price")
    public double price;

    @SerializedName("product")
    public Product product;
}
