package com.example.productsaleandroid.models;

public class WishlistRequest {
    private int productId;

    public WishlistRequest(int productId) {
        this.productId = productId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}