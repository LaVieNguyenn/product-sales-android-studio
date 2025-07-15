package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.AddToCartRequest;
import com.example.productsaleandroid.models.Cart;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.DELETE;
import retrofit2.http.POST;

public interface CartApi {
    @GET("api/carts")
    Call<Cart> getCurrentCart(@Header("Authorization") String token);

    @PUT("api/carts/items/{id}")
    Call<Void> updateCartItemQuantity(
            @Header("Authorization") String token,
            @Path("id") int cartItemId,
            @Body UpdateQuantityRequest req
    );
    @DELETE("api/carts/items/{id}")
    Call<Void> deleteCartItem(
            @Header("Authorization") String token,
            @Path("id") int cartItemId
    );
    @POST("/api/carts/items")
    Call<Void> addToCart(
            @Header("Authorization") String token,
            @Body AddToCartRequest request
    );
    @DELETE("/api/carts")
    Call<Void> clearCart(@Header("Authorization") String token);
}
