package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.OrderCreateResponse;
import com.example.productsaleandroid.models.OrderDetail;
import com.example.productsaleandroid.models.OrderDetailResponse;
import com.example.productsaleandroid.models.OrderRequest;
import com.example.productsaleandroid.models.OrderSummary;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderApi {
    @POST("api/orders")
    Call<OrderCreateResponse> createOrder(
            @Header("Authorization") String bearerToken,
            @Body OrderRequest orderRequest
    );
    @GET("/api/orders/{id}")
    Call<OrderDetailResponse> getOrderById(
            @Header("Authorization") String bearerToken,
            @Path("id") int orderId
    );
    @GET("/api/orders/me")
    Call<List<OrderSummary>> getMyOrders(@Header("Authorization") String token);

    @GET("/api/orders/{id}")
    Call<OrderDetail> getOrderByIdDetail(@Header("Authorization") String token, @Path("id") int orderId);
}
