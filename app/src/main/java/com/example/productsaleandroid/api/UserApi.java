package com.example.productsaleandroid.api;
import com.example.productsaleandroid.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
public interface UserApi {
    @GET("/api/users/me")
    Call<User> getMe(@Header("Authorization") String bearerToken);
    @PUT("/api/users/me")
    Call<User> updateMe(@Header("Authorization") String token, @Body User user);
}
