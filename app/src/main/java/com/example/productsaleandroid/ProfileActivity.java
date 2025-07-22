package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.CartApi;
import com.example.productsaleandroid.api.UserApi;
import com.example.productsaleandroid.models.Cart;
import com.example.productsaleandroid.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnSettings;
    private TextView tvUserName;

    // Không khai báo trực tiếp cartBadge, mà lấy từ include!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnSettings = findViewById(R.id.btnSettings);
        tvUserName = findViewById(R.id.tvUserName);

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });


        // Lấy token để gọi API user & cart
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thông tin user
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        userApi.getMe("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvUserName.setText(user.getUsername());
                } else {
                    Toast.makeText(ProfileActivity.this, "Không thể lấy thông tin user!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });

        // **Lấy badge từ layout include**
        View bottomNav = findViewById(R.id.includeBottomNav);
        TextView cartBadge = bottomNav.findViewById(R.id.tvCartBadge);

        // Lấy số lượng sản phẩm trong giỏ để hiển thị badge
        loadCartCount(token, cartBadge);

        // Footer navigation
        bottomNav.findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        bottomNav.findViewById(R.id.nav_favorite).setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
        bottomNav.findViewById(R.id.nav_cart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
            finish();
        });
        bottomNav.findViewById(R.id.nav_profile).setOnClickListener(v -> {
            // Đang ở Profile, không cần gì thêm
        });
    }

    // Nhận cartBadge làm đối số
    private void loadCartCount(String token, TextView cartBadge) {
        CartApi cartApi = ApiClient.getClient().create(CartApi.class);
        cartApi.getCurrentCart("Bearer " + token).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                    int count = response.body().items.size();
                    if (count > 0) {
                        cartBadge.setVisibility(View.VISIBLE);
                        cartBadge.setText(String.valueOf(count));
                    } else {
                        cartBadge.setVisibility(View.GONE);
                    }
                } else {
                    cartBadge.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                cartBadge.setVisibility(View.GONE);
            }
        });
    }
}
