package com.example.productsaleandroid.models;

import com.google.gson.annotations.SerializedName;

public class OrderDetail {
    public int orderId;
    public String paymentMethod;
    public String billingAddress;
    public String orderStatus;
    public Cart cart;
}
