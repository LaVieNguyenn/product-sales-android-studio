package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProductApi {
    @GET("/api/products")
    Call<List<Product>> getAllProducts();
}
