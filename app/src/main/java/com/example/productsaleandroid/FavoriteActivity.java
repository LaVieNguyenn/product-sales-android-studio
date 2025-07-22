package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.CartApi;
import com.example.productsaleandroid.api.WishlistApi;
import com.example.productsaleandroid.models.Cart;
import com.example.productsaleandroid.models.WishlistItem;
import com.example.productsaleandroid.models.WishlistListResponse;
import com.example.productsaleandroid.models.WishlistResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private WishlistAdapter adapter;
    private List<WishlistItem> wishlist = new ArrayList<>();
    private WishlistApi wishlistApi;

    private TextView tvCartBadge;
    private FrameLayout btnCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WishlistAdapter(wishlist, this);
        recyclerView.setAdapter(adapter);

        // Khởi tạo API
        wishlistApi = ApiClient.getClient().create(WishlistApi.class);

        // Back về Home
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Badge cart
        tvCartBadge = findViewById(R.id.tvCartBadge);
        btnCart = findViewById(R.id.btnCart);
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Gọi API
        loadWishlist();
        loadCartBadgeFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartBadgeFromApi();
    }

    private void loadWishlist() {
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("token", "");
        Log.d("FAV_DEBUG", "Token: " + token);

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Token rỗng! Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            Log.e("FAV_DEBUG", "Không có token để gọi API.");
            return;
        }

        wishlistApi.getWishlist("Bearer " + token).enqueue(new Callback<WishlistListResponse>() {
            @Override
            public void onResponse(Call<WishlistListResponse> call, Response<WishlistListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wishlist.clear();
                    wishlist.addAll(response.body().getData()); // ✅ addAll danh sách wishlist
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("FAV_DEBUG", "Response lỗi hoặc body null. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WishlistListResponse> call, Throwable t) {
                Log.e("FAV_DEBUG", "API thất bại: " + t.getMessage());
            }
        });
    }


    private void loadCartBadgeFromApi() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        CartApi cartApi = ApiClient.getClient().create(CartApi.class);
        cartApi.getCurrentCart("Bearer " + token).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                    updateCartBadge(response.body().items.size());
                } else {
                    updateCartBadge(0);
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                updateCartBadge(0);
            }
        });
    }

    private void updateCartBadge(int count) {
        if (tvCartBadge != null) {
            if (count > 0) {
                tvCartBadge.setText(String.valueOf(count));
                tvCartBadge.setVisibility(View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }
}
