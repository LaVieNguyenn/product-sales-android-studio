package com.example.productsaleandroid.models;

public class PaypalConfirmRequest {
    public String paypalOrderId;
    public int orderId;
    public PaypalConfirmRequest(String paypalOrderId, int orderId) {
        this.paypalOrderId = paypalOrderId;
        this.orderId = orderId;
    }
}