package com.example.productsaleandroid.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderDetail {
    public int orderId;
    public String paymentMethod;
    public String billingAddress;
    public String orderStatus;
    public Cart cart;
    public static class Cart {
        public int cartId;
        public int userId;
        public double totalPrice;
        public String status;
        public double finalPrice;
        public List<CartItem> cartItems;
    }
}
