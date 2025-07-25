package com.example.productsaleandroid.models;

public class ApplyVoucherResponse {
    public String message;
    public Cart cart;
    public static class Cart {
        public int cartId;
        public int userId;
        public double totalPrice;
        public String status;
        public String code;

        public int voucherId;
        public double discountAmount;
        public double finalPrice;
    }
}

