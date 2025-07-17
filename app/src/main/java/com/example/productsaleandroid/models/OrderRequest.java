package com.example.productsaleandroid.models;

public class OrderRequest {
    public String paymentMethod;
    public String billingAddress;

    public OrderRequest(String paymentMethod, String billingAddress) {
        this.paymentMethod = paymentMethod;
        this.billingAddress = billingAddress;
    }
}