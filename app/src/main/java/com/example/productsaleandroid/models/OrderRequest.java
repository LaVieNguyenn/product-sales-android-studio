package com.example.productsaleandroid.models;

import java.util.List;

public class OrderRequest {
    public String paymentMethod;
    public String billingAddress;
    public List<Integer> cartItemIds; // <-- thêm dòng này

    public OrderRequest(String paymentMethod, String billingAddress, List<Integer> cartItemIds) {
        this.paymentMethod = paymentMethod;
        this.billingAddress = billingAddress;
        this.cartItemIds = cartItemIds;
    }
}
