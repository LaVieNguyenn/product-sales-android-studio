package com.example.productsaleandroid.models;
import com.google.gson.annotations.SerializedName;
public class Product {
    @SerializedName("ProductID")
    private int productID;

    @SerializedName("ProductName")
    private String productName;

    @SerializedName("BriefDescription")
    private String briefDescription;

    @SerializedName("FullDescription")
    private String fullDescription;

    @SerializedName("TechnicalSpecifications")
    private String technicalSpecifications;

    @SerializedName("Price")
    private double price;

    @SerializedName("ImageURL")
    private String imageURL;
    @SerializedName("CategoryID")
    private int categoryID;
    // Constructor
    public Product(int productID, String productName, String briefDescription,
                   String fullDescription, String technicalSpecifications,
                   double price, String imageURL, int categoryID) {
        this.productID = productID;
        this.productName = productName;
        this.briefDescription = briefDescription;
        this.fullDescription = fullDescription;
        this.technicalSpecifications = technicalSpecifications;
        this.price = price;
        this.imageURL = imageURL;
        this.categoryID = categoryID;
    }

    // Getters
    public int getProductID() { return productID; }
    public String getProductName() { return productName; }
    public String getBriefDescription() { return briefDescription; }
    public String getFullDescription() { return fullDescription; }
    public String getTechnicalSpecifications() { return technicalSpecifications; }
    public double getPrice() { return price; }
    public String getImageURL() { return imageURL; }
    public int getCategoryID() { return categoryID; }
}
