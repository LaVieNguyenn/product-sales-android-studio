package com.example.productsaleandroid.models;

public class AddToCartRequest {
    public int productId;
    public int quantity;
    public AddToCartRequest(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}