// ChatApi.java
package com.example.productsaleandroid.api;

import com.example.productsaleandroid.models.ChatMessage;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ChatApi {
    @GET("/api/chat/messages")
    Call<List<ChatMessage>> getMessages(
            @Header("Authorization") String token,
            @Query("otherUserId") int otherUserId
    );
}
