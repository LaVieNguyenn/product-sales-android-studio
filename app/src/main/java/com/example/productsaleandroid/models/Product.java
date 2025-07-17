package com.example.productsaleandroid.models;

import java.io.Serializable;

public class Product implements Serializable {
    private int productId;

    private String productName;

    private String briefDescription;

    private String fullDescription;

    private String technicalSpecifications;

    private double price;

    private String imageUrl;
    private int categoryId;
    // Constructor
    public Product(int productID, String productName, String briefDescription,
                   String fullDescription, String technicalSpecifications,
                   double price, String imageURL, int categoryID) {
        this.productId = productID;
        this.productName = productName;
        this.briefDescription = briefDescription;
        this.fullDescription = fullDescription;
        this.technicalSpecifications = technicalSpecifications;
        this.price = price;
        this.imageUrl = imageURL;
        this.categoryId = categoryID;
    }

    // Getters
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getBriefDescription() { return briefDescription; }
    public String getFullDescription() { return fullDescription; }
    public String getTechnicalSpecifications() { return technicalSpecifications; }
    public double getPrice() { return price; }
    public String getImageURL() { return imageUrl; }
    public int getCategoryId() { return categoryId; }
}
