package com.example.productsaleandroid.models;

public class WishlistItem {
    private int wishlistId;
    private int productId;
    private Product product;
    private boolean isSelected = false;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}