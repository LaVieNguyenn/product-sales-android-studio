package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.PaypalCheckoutRequest;
import com.example.productsaleandroid.models.PaypalCheckoutResponse;
import com.example.productsaleandroid.models.PaypalConfirmRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface PaymentApi {
    @POST("api/payments/checkout")
    Call<PaypalCheckoutResponse> checkoutPaypal(@Header("Authorization") String token, @Body PaypalCheckoutRequest request);

    @POST("api/payments/confirm")
    Call<Void> confirmPaypal(@Header("Authorization") String token, @Body PaypalConfirmRequest req);
}