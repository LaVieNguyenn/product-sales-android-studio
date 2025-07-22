package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.WishlistItem;
import com.example.productsaleandroid.models.WishlistListResponse;
import com.example.productsaleandroid.models.WishlistRequest;
import com.example.productsaleandroid.models.WishlistResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WishlistApi {
    @GET("api/wishlist")
    Call<WishlistListResponse> getWishlist(@Header("Authorization") String token);
    @POST("api/wishlist")
    Call<WishlistResponse> addToWishlist(
            @Header("Authorization") String token,
            @Body WishlistRequest request
    );
    @DELETE("/api/wishlist/{productId}")
    Call<Void> removeFromWishlist(@Header("Authorization") String token, @Path("productId") int productId);
}
