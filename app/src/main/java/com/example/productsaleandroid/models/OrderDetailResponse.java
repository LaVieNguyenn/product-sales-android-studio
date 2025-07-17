package com.example.productsaleandroid.models;

import java.util.List;

public class OrderDetailResponse {
    public int orderId;
    public int cartId;
    public int userId;
    public String paymentMethod;
    public String billingAddress;
    public String orderStatus;
    public String orderDate;
    public Cart cart;

    public static class Cart {
        public int cartId;
        public int userId;
        public double totalPrice;
        public String status;
        public List<CartItem> cartItems;
    }
}
