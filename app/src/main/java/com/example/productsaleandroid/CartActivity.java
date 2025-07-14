package com.example.productsaleandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.productsaleandroid.api.CartApi;
import com.example.productsaleandroid.models.Cart;
import com.example.productsaleandroid.models.CartItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;

    // Header
    private ImageView btnBack;
    private TextView tvCartTitle, tvEdit, tvChatBadge;

    // Footer mặc định
    private LinearLayout footerCart;
    private CheckBox cbSelectAll;
    private TextView tvFooterTotal, tvFooterBuy;

    // Footer edit mode
    private LinearLayout footerCartEdit;
    private CheckBox cbSelectAllEdit;
    private TextView btnSaveToFav, btnDelete;

    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Header
        btnBack = findViewById(R.id.btnBack);
        tvCartTitle = findViewById(R.id.tvCartTitle);
        tvEdit = findViewById(R.id.tvEdit);
        tvChatBadge = findViewById(R.id.tvChatBadge);

        btnBack.setOnClickListener(v -> finish());

        // Footer mặc định
        footerCart = findViewById(R.id.footerCart);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        tvFooterTotal = findViewById(R.id.tvFooterTotal);
        tvFooterBuy = findViewById(R.id.tvFooterBuy);

        // Footer edit mode
        footerCartEdit = findViewById(R.id.footerCartEdit);
        cbSelectAllEdit = findViewById(R.id.cbSelectAllEdit);
        btnSaveToFav = findViewById(R.id.btnSaveToFav);
        btnDelete = findViewById(R.id.btnDelete);

        // Mặc định footer edit ẩn
        footerCartEdit.setVisibility(View.GONE);

        tvEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            tvEdit.setText(isEditMode ? "Xong" : "Sửa");
            footerCart.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
            footerCartEdit.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
            // Nếu adapter có cờ editMode thì update luôn
            if (cartAdapter != null) {
                cartAdapter.setEditMode(isEditMode);
            }
        });

        int chatCount = 19;
        tvChatBadge.setText(String.valueOf(chatCount));
        tvChatBadge.setVisibility(chatCount > 0 ? View.VISIBLE : View.GONE);

        // RecyclerView
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        // Lấy token
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Adapter phải tạo sau khi có token!
        cartAdapter = new CartAdapter(this, new ArrayList<>(), token);
        recyclerViewCart.setAdapter(cartAdapter);
        cartAdapter.setOnItemCheckedChangeListener(() -> {
            updateFooter();
            updateCartTitle();
        });

        // Chọn tất cả (footer mặc định)
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cartAdapter.getItems() == null) return;
            for (CartItem item : cartAdapter.getItems()) {
                item.isSelected = isChecked;
            }
            cartAdapter.notifyDataSetChanged();
            updateFooter();
        });

        // Chọn tất cả (footer edit mode)
        cbSelectAllEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (cartAdapter.getItems() == null) return;
            for (CartItem item : cartAdapter.getItems()) {
                item.isSelected = isChecked;
            }
            cartAdapter.notifyDataSetChanged();
            updateFooter();
        });

        tvFooterBuy.setOnClickListener(v -> {
            int count = getSelectedCount();
            if (count == 0) {
                Toast.makeText(this, "Hãy chọn sản phẩm để mua!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Xử lý đặt hàng ở đây (danh sách sản phẩm đã chọn)
            Toast.makeText(this, "Bạn đã chọn " + count + " sản phẩm.", Toast.LENGTH_SHORT).show();
        });

        btnSaveToFav.setOnClickListener(v -> {
            int count = getSelectedCount();
            if (count == 0) {
                Toast.makeText(this, "Chọn sản phẩm để lưu!", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: API lưu vào đã thích ở đây
            Toast.makeText(this, "Đã lưu " + count + " sản phẩm vào Đã thích!", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            int count = getSelectedCount();
            if (count == 0) {
                Toast.makeText(this, "Chọn sản phẩm để xóa!", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: API xóa sản phẩm khỏi giỏ hàng ở đây
            Toast.makeText(this, "Đã xóa " + count + " sản phẩm!", Toast.LENGTH_SHORT).show();
        });

        loadCartFromApi(token);
    }

    private void updateCartTitle() {
        int cartCount = cartAdapter.getItems() != null ? cartAdapter.getItems().size() : 0;
        if (cartCount > 0) {
            tvCartTitle.setText("Giỏ hàng (" + cartCount + ")");
        } else {
            tvCartTitle.setText("Giỏ hàng");
        }
    }

    private void loadCartFromApi(String token) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartApi cartApi = retrofit.create(CartApi.class);
        Call<Cart> call = cartApi.getCurrentCart("Bearer " + token);

        call.enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Cart cart = response.body();
                    if (cart.items != null) {
                        for (CartItem item : cart.items) item.isSelected = false;
                    }
                    cartAdapter.setItems(cart.items);
                    updateFooter();
                } else {
                    Toast.makeText(CartActivity.this, "Không thể lấy giỏ hàng!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Đếm số sản phẩm đã chọn
    private int getSelectedCount() {
        int count = 0;
        List<CartItem> items = cartAdapter.getItems();
        if (items != null) {
            for (CartItem item : items) if (item.isSelected) count++;
        }
        return count;
    }

    // Tổng tiền đã chọn
    private double getSelectedTotal() {
        double total = 0;
        List<CartItem> items = cartAdapter.getItems();
        if (items != null) {
            for (CartItem item : items) if (item.isSelected) total += item.price * item.quantity;
        }
        return total;
    }

    // Cập nhật footer phù hợp mode
    private void updateFooter() {
        int selectedCount = getSelectedCount();
        double selectedTotal = getSelectedTotal();

        if (!isEditMode) {
            tvFooterTotal.setText("Tổng cộng đ" + String.format("%,.0f", selectedTotal));
            tvFooterBuy.setText("Mua hàng (" + selectedCount + ")");
            // Update "chọn tất cả" ở footer mặc định
            List<CartItem> items = cartAdapter.getItems();
            boolean allChecked = true;
            if (items != null && !items.isEmpty()) {
                for (CartItem item : items) {
                    if (!item.isSelected) {
                        allChecked = false;
                        break;
                    }
                }
                cbSelectAll.setOnCheckedChangeListener(null);
                cbSelectAll.setChecked(allChecked);
                cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    for (CartItem item : cartAdapter.getItems()) {
                        item.isSelected = isChecked;
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateFooter();
                });
            } else {
                cbSelectAll.setOnCheckedChangeListener(null);
                cbSelectAll.setChecked(false);
                cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    for (CartItem item : cartAdapter.getItems()) {
                        item.isSelected = isChecked;
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateFooter();
                });
            }
        } else {
            // Update "chọn tất cả" ở footer edit
            List<CartItem> items = cartAdapter.getItems();
            boolean allChecked = true;
            if (items != null && !items.isEmpty()) {
                for (CartItem item : items) {
                    if (!item.isSelected) {
                        allChecked = false;
                        break;
                    }
                }
                cbSelectAllEdit.setOnCheckedChangeListener(null);
                cbSelectAllEdit.setChecked(allChecked);
                cbSelectAllEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    for (CartItem item : cartAdapter.getItems()) {
                        item.isSelected = isChecked;
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateFooter();
                });
            } else {
                cbSelectAllEdit.setOnCheckedChangeListener(null);
                cbSelectAllEdit.setChecked(false);
                cbSelectAllEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    for (CartItem item : cartAdapter.getItems()) {
                        item.isSelected = isChecked;
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateFooter();
                });
            }
        }
    }
}
