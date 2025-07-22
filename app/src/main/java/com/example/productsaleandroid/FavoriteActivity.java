package com.example.productsaleandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.CartApi;
import com.example.productsaleandroid.api.WishlistApi;
import com.example.productsaleandroid.models.Cart;
import com.example.productsaleandroid.models.WishlistItem;
import com.example.productsaleandroid.models.WishlistListResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private boolean isEditMode = false;
    private TextView btnEdit;
    private LinearLayout selectionBar;
    private Button btnRemoveSelected;
    private CheckBox checkboxAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WishlistAdapter(wishlist, this);
        recyclerView.setAdapter(adapter);

        wishlistApi = ApiClient.getClient().create(WishlistApi.class);

        // Back về Home
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        // Giỏ hàng
        tvCartBadge = findViewById(R.id.tvCartBadge);
        btnCart = findViewById(R.id.btnCart);
        btnCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        // Chỉnh sửa
        btnEdit = findViewById(R.id.btnEdit);
        selectionBar = findViewById(R.id.selectionBar);
        btnRemoveSelected = findViewById(R.id.btnRemoveSelected);
        checkboxAll = findViewById(R.id.checkboxAll);

        btnEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            adapter.setEditMode(isEditMode);
            btnEdit.setText(isEditMode ? "Hoàn tất" : "Chỉnh sửa");
            selectionBar.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
            adapter.clearSelections();
        });

        btnRemoveSelected.setOnClickListener(v -> {
            Set<Integer> selected = adapter.getSelectedProductIds();
            if (selected.isEmpty()) {
                Toast.makeText(this, "Chưa chọn sản phẩm nào", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn bỏ thích " + selected.size() + " sản phẩm?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("token", "");
                        for (Integer id : selected) {
                            wishlistApi.removeFromWishlist("Bearer " + token, id).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    loadWishlist();
                                }
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(FavoriteActivity.this, "Lỗi khi xoá!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        checkboxAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
            updateSelectedCount();
        });

        adapter.setOnSelectionChangedListener(() -> updateSelectedCount());

        loadWishlist();
        loadCartBadgeFromApi();
    }

    private void updateSelectedCount() {
        int selected = adapter.getSelectedProductIds().size();
        int total = wishlist != null ? wishlist.size() : 0;
        checkboxAll.setText("Đã chọn " + selected + "/" + total);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartBadgeFromApi();
    }

    private void loadWishlist() {
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("token", "");

        wishlistApi.getWishlist("Bearer " + token).enqueue(new Callback<WishlistListResponse>() {
            @Override
            public void onResponse(Call<WishlistListResponse> call, Response<WishlistListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wishlist.clear();
                    wishlist.addAll(response.body().getData());
                    adapter.clearSelections(); // 🛠️ Thêm dòng này để reset lại selectedIds
                    adapter.notifyDataSetChanged();
                    updateSelectedCount();
                }
            }

            @Override
            public void onFailure(Call<WishlistListResponse> call, Throwable t) {
                Log.e("FAV_DEBUG", "API thất bại: " + t.getMessage());
            }
        });
    }



    private void loadCartBadgeFromApi() {
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("token", "");
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
