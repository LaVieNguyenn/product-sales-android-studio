package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.GoogleLoginRequest;
import com.example.productsaleandroid.models.LoginRequest;
import com.example.productsaleandroid.models.LoginResponse;
import com.example.productsaleandroid.models.UserRegisterRequest;
import com.example.productsaleandroid.models.UserRegisterResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @POST("/api/auth/google")
    Call<LoginResponse> loginWithGoogle(@Body GoogleLoginRequest request);
    @GET("/api/auth/log-out")
    Call<Void> logout(@Header("Authorization") String token);
    @POST("/api/auth/register")
    Call<UserRegisterResponse> register(@Body UserRegisterRequest req);

    @GET("/api/auth/verify-email")
    Call<Void> verifyEmail(@Query("token") String token);
}
