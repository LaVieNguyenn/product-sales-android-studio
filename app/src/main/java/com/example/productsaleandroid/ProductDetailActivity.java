package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.productsaleandroid.api.CartApi;
import com.example.productsaleandroid.models.AddToCartRequest;
import com.example.productsaleandroid.models.Cart;
import com.example.productsaleandroid.api.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    ImageView imgProductDetail;
    TextView tvProductNameDetail, tvProductPriceDetail, tvProductDescriptionDetail, tvTechnicalSpecification;
    ImageView btnBack;
    TextView tvCartBadge;

    String name, technicalSpecification, description, imageUrl;
    double price;
    int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        imgProductDetail = findViewById(R.id.imgProductDetail);
        tvProductNameDetail = findViewById(R.id.tvProductNameDetail);
        tvTechnicalSpecification = findViewById(R.id.tvTechnicalSpecification);
        tvProductPriceDetail = findViewById(R.id.tvProductPriceDetail);
        tvProductDescriptionDetail = findViewById(R.id.tvProductDescriptionDetail);
        btnBack = findViewById(R.id.btnBack);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        // --- Nhận dữ liệu từ Intent ---
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        technicalSpecification = intent.getStringExtra("technicalSpecification");
        description = intent.getStringExtra("description");
        price = intent.getDoubleExtra("price", 0);
        imageUrl = intent.getStringExtra("image");
        productId = intent.getIntExtra("productId", 0);

        // Hiển thị dữ liệu
        tvProductNameDetail.setText(name);
        tvProductDescriptionDetail.setText(description);
        tvTechnicalSpecification.setText(technicalSpecification);
        tvProductPriceDetail.setText(String.format("₫%,.0f VND", price));
        TextView tvVoucherPrice = findViewById(R.id.tvVoucherPrice);
        tvVoucherPrice.setText(String.format("₫%,.0f", price));
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(imgProductDetail);

        btnBack.setOnClickListener(v -> finish());

        // Footer: click "Thêm vào Giỏ hàng"
        findViewById(R.id.footerAction).findViewById(R.id.footer_add_to_cart)
                .setOnClickListener(view -> showAddToCartSheet());

        // Khi vào màn hình -> load badge
        loadCartBadgeFromApi();
        View cartContainer = findViewById(R.id.cartContainer); // FrameLayout bọc icon cart + badge
        cartContainer.setOnClickListener(v -> {
            Intent intentCart = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intentCart);
        });
    }

    /*** GỌI API LẤY SỐ LƯỢNG GIỎ HÀNG ***/
    private void loadCartBadgeFromApi() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token.isEmpty()) {
            updateCartBadge(0);
            return;
        }
        CartApi api = ApiClient.getClient().create(CartApi.class);
        api.getCurrentCart("Bearer " + token).enqueue(new Callback<Cart>() {
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

    /*** HIỂN THỊ SỐ LƯỢNG TRÊN BADGE ***/
    private void updateCartBadge(int count) {
        if (tvCartBadge == null) return;
        if (count > 0) {
            tvCartBadge.setVisibility(View.VISIBLE);
            tvCartBadge.setText(String.valueOf(count));
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    /*** BOTTOM SHEET CHỌN SỐ LƯỢNG ***/
    private void showAddToCartSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_to_cart, null);
        dialog.setContentView(view);

        ImageView imgProduct = view.findViewById(R.id.imgSheetProduct);
        TextView tvName = view.findViewById(R.id.tvSheetName);
        TextView tvPrice = view.findViewById(R.id.tvSheetPrice);
        ImageView btnMinus = view.findViewById(R.id.btnMinus);
        ImageView btnPlus = view.findViewById(R.id.btnPlus);
        TextView tvQuantity = view.findViewById(R.id.tvQuantity);
        Button btnConfirm = view.findViewById(R.id.btnConfirmAddToCart);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(imgProduct);
        tvName.setText(name);
        tvPrice.setText(String.format("₫%,.0f VND", price));

        final int[] quantity = {1};
        tvQuantity.setText(String.valueOf(quantity[0]));

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                tvQuantity.setText(String.valueOf(quantity[0]));
            }
        });
        btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            tvQuantity.setText(String.valueOf(quantity[0]));
        });

        btnConfirm.setOnClickListener(v -> {
            addToCart(quantity[0], dialog);
        });

        dialog.show();
    }

    /*** GỌI API THÊM VÀO GIỎ VÀ REFRESH BADGE ***/
    private void addToCart(int quantity, BottomSheetDialog dialog) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        AddToCartRequest req = new AddToCartRequest(productId, quantity);
        CartApi api = ApiClient.getClient().create(CartApi.class);
        api.addToCart("Bearer " + token, req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Không cộng biến local, gọi lại API lấy đúng badge từ server
                    loadCartBadgeFromApi();
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) errorBody = response.errorBody().string();
                    } catch (Exception e) {
                        android.util.Log.e("API_ADD_TO_CART", "Error parsing errorBody: " + e.getMessage());
                    }
                    Toast.makeText(ProductDetailActivity.this, "Thêm vào giỏ hàng thất bại!", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("API_ADD_TO_CART", "FAIL | code: " + response.code() +
                            ", token=" + token + ", productId=" + productId + ", quantity=" + quantity +
                            ", errorBody=" + errorBody);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                android.util.Log.e("API_ADD_TO_CART", "FAIL | Lỗi kết nối: " + t.getMessage(), t);
            }
        });
    }
}
