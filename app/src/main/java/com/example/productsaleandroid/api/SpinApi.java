package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.SpinResult;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SpinApi {
    @POST("/api/spin") // phải có /api/spin, không phải /spin
    Call<SpinResult> spin(@Header("Authorization") String token);
}