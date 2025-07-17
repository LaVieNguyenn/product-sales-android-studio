package com.example.productsaleandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.LinearLayout;
import android.content.Intent;

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

import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;

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
    private String token = "";

    // Request code xin quyền notification
    private static final int NOTIF_PERMISSION_REQUEST = 1011;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Xin quyền notification cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIF_PERMISSION_REQUEST);
            }
        }

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
        tvFooterBuy = findViewById(R.id.tvFooterBuy); // Nút "Mua hàng"

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
        token = prefs.getString("token", "");
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

        // NÚT "Mua hàng" → Mở PaymentActivity và truyền data
        tvFooterBuy.setOnClickListener(v -> {
            List<CartItem> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems == null || selectedItems.isEmpty()) {
                Toast.makeText(this, "Hãy chọn sản phẩm để mua!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, PaymentActivity.class);

            // Nếu chỉ 1 sản phẩm thì truyền 1 item, còn nhiều thì truyền list (tuỳ ý bạn)
            if (selectedItems.size() == 1) {
                intent.putExtra("cartItem", selectedItems.get(0)); // CartItem implements Serializable
            } else {
                intent.putExtra("selectedItems", new ArrayList<>(selectedItems)); // ArrayList implements Serializable
            }

            double total = 0;
            for (CartItem item : selectedItems) total += item.price * item.quantity;
            intent.putExtra("totalAmount", total);

            startActivity(intent);
        });

        btnSaveToFav.setOnClickListener(v -> {
            int count = getSelectedCount();
            if (count == 0) {
                Toast.makeText(this, "Chọn sản phẩm để lưu!", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Đã lưu " + count + " sản phẩm vào Đã thích!", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            List<CartItem> selectedItems = cartAdapter.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Chọn sản phẩm để xóa!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedItems.size() == cartAdapter.getItems().size()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setMessage("Bạn muốn xóa toàn bộ sản phẩm trong giỏ?")
                        .setNegativeButton("Không", null)
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            deleteAllCartItems(token);
                        })
                        .show();
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setMessage("Bạn muốn xóa các sản phẩm đã chọn?")
                        .setNegativeButton("Không", null)
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            deleteSelectedCartItems(token, selectedItems);
                        })
                        .show();
            }
        });

        loadCartFromApi(token);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIF_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (cartAdapter != null && cartAdapter.getItems() != null && !cartAdapter.getItems().isEmpty()) {
                    NotificationUtils.showCartBadgeNotification(this, cartAdapter.getItems().size());
                }
            }
        }
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
                    int cartCount = cart.items != null ? cart.items.size() : 0;
                    if (cartCount > 0) {
                        NotificationUtils.showCartBadgeNotification(CartActivity.this, cartCount);
                    } else {
                        NotificationUtils.cancelCartBadge(CartActivity.this);
                    }
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

    private int getSelectedCount() {
        int count = 0;
        List<CartItem> items = cartAdapter.getItems();
        if (items != null) {
            for (CartItem item : items) if (item.isSelected) count++;
        }
        return count;
    }

    private double getSelectedTotal() {
        double total = 0;
        List<CartItem> items = cartAdapter.getItems();
        if (items != null) {
            for (CartItem item : items) if (item.isSelected) total += item.price * item.quantity;
        }
        return total;
    }

    private void updateFooter() {
        int selectedCount = getSelectedCount();
        double selectedTotal = getSelectedTotal();

        if (!isEditMode) {
            tvFooterTotal.setText("Tổng cộng đ" + String.format("%,.0f", selectedTotal));
            tvFooterBuy.setText("Mua hàng (" + selectedCount + ")");
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

    private void deleteAllCartItems(String token) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartApi cartApi = retrofit.create(CartApi.class);
        cartApi.clearCart("Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cartAdapter.setItems(new ArrayList<>());
                    Toast.makeText(CartActivity.this, "Đã xóa toàn bộ sản phẩm!", Toast.LENGTH_SHORT).show();
                    updateFooter();
                    updateCartTitle();
                    NotificationUtils.cancelCartBadge(CartActivity.this);
                } else {
                    Toast.makeText(CartActivity.this, "Không thể xóa giỏ hàng!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSelectedCartItems(String token, List<CartItem> selectedItems) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartApi cartApi = retrofit.create(CartApi.class);

        int[] counter = {0};
        for (CartItem item : selectedItems) {
            cartApi.deleteCartItem("Bearer " + token, item.cartItemID).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    counter[0]++;
                    if (counter[0] == selectedItems.size()) {
                        loadCartFromApi(token);
                        Toast.makeText(CartActivity.this, "Đã xóa sản phẩm đã chọn!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    counter[0]++;
                    if (counter[0] == selectedItems.size()) {
                        loadCartFromApi(token);
                        Toast.makeText(CartActivity.this, "Đã xóa sản phẩm đã chọn!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
