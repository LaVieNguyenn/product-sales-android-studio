package com.example.productsaleandroid.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderCart {
    @SerializedName("cartId")
    public int cartId;

    @SerializedName("userId")
    public int userId;

    @SerializedName("totalPrice")
    public int totalPrice;

    @SerializedName("status")
    public String status;

    @SerializedName("cartItems")
    public List<OrderCartItem> cartItems;
}
