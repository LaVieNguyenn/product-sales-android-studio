package com.example.productsaleandroid.api;

import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.AuthService;
import com.example.productsaleandroid.models.LoginRequest;
import com.example.productsaleandroid.models.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;

public class AuthRepository {

    private final AuthService authService;

    public AuthRepository() {
        authService = ApiClient.getClient().create(AuthService.class);
    }

    public void login(String username, String password, Callback<LoginResponse> callback) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<LoginResponse> call = authService.login(loginRequest);
        call.enqueue(callback);
    }
}
