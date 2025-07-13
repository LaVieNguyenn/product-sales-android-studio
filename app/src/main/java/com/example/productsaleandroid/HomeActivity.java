package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.CartApi;
import com.example.productsaleandroid.api.ProductApi;
import com.example.productsaleandroid.models.Cart;
import com.example.productsaleandroid.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout bottomNav;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Bottom Navigation
        bottomNav = findViewById(R.id.includeBottomNav);

        bottomNav.findViewById(R.id.nav_home).setOnClickListener(v ->
                Toast.makeText(this, "Đang ở trang chủ", Toast.LENGTH_SHORT).show());

        bottomNav.findViewById(R.id.nav_search).setOnClickListener(v ->
                Toast.makeText(this, "Tìm kiếm", Toast.LENGTH_SHORT).show());

        bottomNav.findViewById(R.id.nav_cart).setOnClickListener(v -> {
            // Chuyển sang trang CartActivity
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });

        bottomNav.findViewById(R.id.nav_profile).setOnClickListener(v ->
                Toast.makeText(this, "Tài khoản", Toast.LENGTH_SHORT).show());

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, null);
        recyclerView.setAdapter(adapter);
        loadCartBadgeFromApi();
        // Gọi API để lấy danh sách sản phẩm
        loadProducts();
    }
    private void loadCartBadgeFromApi() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartApi cartApi = retrofit.create(CartApi.class);
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

    private void updateCartBadge(int cartCount) {
        TextView tvCartBadge = bottomNav.findViewById(R.id.tvCartBadge);
        if (cartCount > 0) {
            tvCartBadge.setText(String.valueOf(cartCount));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }
    private void loadProducts() {
        ProductApi api = ApiClient.getClient().create(ProductApi.class);
        api.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setProductList(response.body());
                } else {
                    Log.e("API", "Response failed");
                    Toast.makeText(HomeActivity.this, "Lấy sản phẩm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
