package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.LoginRequest;
import com.example.productsaleandroid.models.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
