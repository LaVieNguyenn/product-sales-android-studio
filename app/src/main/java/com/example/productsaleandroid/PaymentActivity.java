package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.productsaleandroid.api.CartApi;
import com.example.productsaleandroid.api.OrderApi;
import com.example.productsaleandroid.api.PaymentApi;
import com.example.productsaleandroid.api.UserApi;
import com.example.productsaleandroid.api.VoucherApi;
import com.example.productsaleandroid.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentActivity extends AppCompatActivity {
    private TextView tvReceiverName, tvReceiverAddress, tvTotal, tvVoucher;
    private Button btnPlaceOrder, btnApplyVoucher;
    private RecyclerView rvSelectedProducts;
    private String token;
    private String paymentMethod = "Paypal";
    private String billingAddress = "";
    private double totalAmount = 0;
    private ArrayList<CartItem> selectedItems;
    private PaymentProductAdapter productAdapter;


    // Lưu userVoucherId đã chọn (nếu có)
    private int selectedUserVoucherId = -1;
    private String appliedVoucherCode = "";

    private static final int REQUEST_VOUCHER = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverAddress = findViewById(R.id.tvReceiverAddress);
        tvTotal = findViewById(R.id.tvTotal);
        tvVoucher = findViewById(R.id.tvVoucher);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        rvSelectedProducts = findViewById(R.id.rvSelectedProducts);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Lấy token từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        // GỌI API LẤY TÊN VÀ ĐỊA CHỈ
        fetchUserInfo();

        // Nhận danh sách sản phẩm từ Intent
        Intent intent = getIntent();
        boolean fromBuyNow = intent.getBooleanExtra("fromBuyNow", false);
        selectedItems = (ArrayList<CartItem>) intent.getSerializableExtra("selectedItems");
        if (selectedItems == null) {
            CartItem item = (CartItem) intent.getSerializableExtra("cartItem");
            selectedItems = new ArrayList<>();
            if (item != null) selectedItems.add(item);
        }

        // Gắn adapter cho RecyclerView
        productAdapter = new PaymentProductAdapter(selectedItems);
        rvSelectedProducts.setAdapter(productAdapter);
        rvSelectedProducts.setLayoutManager(new LinearLayoutManager(this));

        // Tính tổng tiền
        totalAmount = 0;
        for (CartItem item2 : selectedItems) {
            totalAmount += item2.price * item2.quantity;
        }
        tvTotal.setText("₫" + String.format("%,.0f", totalAmount));

        // --- PHÂN BIỆT HAI LUỒNG ---
        if (fromBuyNow && getIntent().hasExtra("cartItem") && selectedItems.size() == 1) {
            // Chỉ gọi addToCart cho "Mua ngay", KHÔNG gọi createOrderAndCheckout ở đây!
            CartItem item = selectedItems.get(0);
            AddToCartRequest req = new AddToCartRequest(item.productID, item.quantity);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://be-allora.onrender.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            CartApi cartApi = retrofit.create(CartApi.class);
            cartApi.addToCart("Bearer " + token, req).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(PaymentActivity.this, "Không thể thêm sản phẩm vào giỏ!", Toast.LENGTH_SHORT).show();
                        // Nếu lỗi thì disable nút Đặt hàng hoặc finish()
                    }
                    // KHÔNG gọi createOrderAndCheckout() ở đây, chỉ addToCart xong!
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(PaymentActivity.this, "Lỗi mạng khi thêm sản phẩm!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Dù là Mua ngay hay từ giỏ đều chỉ gọi createOrderAndCheckout khi bấm nút
        btnPlaceOrder.setOnClickListener(v -> createOrderAndCheckout());

        // ==== Áp dụng voucher ====
        btnApplyVoucher.setOnClickListener(v -> {
            Intent intentVoucher = new Intent(this, VoucherListActivity.class);
            startActivityForResult(intentVoucher, REQUEST_VOUCHER);
        });

        // Giao diện mặc định
        resetVoucherUI();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOUCHER && resultCode == RESULT_OK && data != null) {
            int userVoucherId = data.getIntExtra("userVoucherId", -1);
            String voucherCode = data.getStringExtra("voucherCode");
            if (userVoucherId > 0) {
                selectedUserVoucherId = userVoucherId;
                appliedVoucherCode = voucherCode;
                applyUserVoucherToCart(userVoucherId);
            }
        }
    }

    private void applyUserVoucherToCart(int userVoucherId) {
        // Gọi API /api/carts/apply-user-voucher
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VoucherApi voucherApi = retrofit.create(VoucherApi.class);

        Map<String, Integer> body = new HashMap<>();
        body.put("userVoucherId", userVoucherId);

        voucherApi.applyUserVoucher("Bearer " + token, body)
                .enqueue(new Callback<ApplyVoucherResponse>() {
                    @Override
                    public void onResponse(Call<ApplyVoucherResponse> call, Response<ApplyVoucherResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().cart != null) {
                            ApplyVoucherResponse res = response.body();
                            Toast.makeText(PaymentActivity.this, "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();

                            // Không lấy code từ cart nữa, chỉ lấy từ biến appliedVoucherCode đã lưu!
                            if (appliedVoucherCode != null && !appliedVoucherCode.isEmpty()) {
                                updateVoucherUI(String.valueOf(res.cart.finalPrice), appliedVoucherCode);
                            } else {
                                updateVoucherUI(String.valueOf(res.cart.finalPrice), "");
                            }
                        } else {
                            String msg = "Áp dụng voucher thất bại!";
                            if (response.body() != null && response.body().message != null) {
                                msg = response.body().message;
                            } else if (response.errorBody() != null) {
                                try {
                                    msg = response.errorBody().string();
                                } catch (Exception ignored) {}
                            }
                            Toast.makeText(PaymentActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApplyVoucherResponse> call, Throwable t) {
                        Toast.makeText(PaymentActivity.this, "Lỗi mạng khi áp dụng voucher!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Cập nhật lại UI sau khi áp dụng voucher
    private void updateVoucherUI(String finalPriceStr, String voucherCode) {
        tvVoucher.setText("Đã áp dụng: " + voucherCode);
        btnApplyVoucher.setText("Đã áp dụng");
        btnApplyVoucher.setEnabled(false);

        try {
            double finalPrice = Double.parseDouble(finalPriceStr);
            tvTotal.setText("₫" + String.format("%,.0f", finalPrice));
        } catch (Exception e) {
            tvTotal.setText("₫" + finalPriceStr);
        }
    }

    // Reset lại UI khi chưa áp dụng voucher
    private void resetVoucherUI() {
        tvVoucher.setText("Chưa áp dụng voucher");
        btnApplyVoucher.setText("Áp dụng");
        btnApplyVoucher.setEnabled(true);
        tvTotal.setText("₫" + String.format("%,.0f", totalAmount));
    }

    private void fetchUserInfo() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UserApi userApi = retrofit.create(UserApi.class);

        if (token == null || token.isEmpty()) {
            tvReceiverName.setText("User");
            tvReceiverAddress.setText("Chưa có địa chỉ");
            billingAddress = "";
            return;
        }
        userApi.getMe("Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvReceiverName.setText(user.getUsername() != null ? user.getUsername() : "User");
                    tvReceiverAddress.setText(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Chưa có địa chỉ");
                    billingAddress = user.getAddress();
                } else {
                    tvReceiverName.setText("User");
                    tvReceiverAddress.setText("Chưa có địa chỉ");
                    billingAddress = "";
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                tvReceiverName.setText("User");
                tvReceiverAddress.setText("Chưa có địa chỉ");
                billingAddress = "";
            }
        });
    }

    // Tạo order => lấy orderId => gọi checkout Paypal => nhận approvalUrl => mở WebView
    private void createOrderAndCheckout() {
        if (token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (billingAddress == null || billingAddress.isEmpty()) {
            Toast.makeText(this, "Bạn chưa nhập địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy danh sách cartItemId từ selectedItems
        ArrayList<Integer> cartItemIds = new ArrayList<>();
        if (selectedItems != null) {
            for (CartItem item : selectedItems) {
                // Nếu CartItem có field cartItemID thì lấy field đó, còn nếu chỉ có productID thì lấy productID.
                // Nhưng thông thường với giỏ hàng phải là cartItemID (ID của item trong cart chứ không phải productID)
                cartItemIds.add(item.cartItemID);
            }
        }

        OrderRequest order = new OrderRequest(paymentMethod, billingAddress, cartItemIds);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OrderApi orderApi = retrofit.create(OrderApi.class);
        orderApi.createOrder("Bearer " + token, order).enqueue(new Callback<OrderCreateResponse>() {
            @Override
            public void onResponse(Call<OrderCreateResponse> call, Response<OrderCreateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int orderId = response.body().orderId;
                    startPaypalCheckout(orderId);
                } else {
                    Toast.makeText(PaymentActivity.this, "Không thể tạo đơn hàng!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<OrderCreateResponse> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startPaypalCheckout(int orderId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        PaypalCheckoutRequest req = new PaypalCheckoutRequest(orderId);

        paymentApi.checkoutPaypal("Bearer " + token, req)
                .enqueue(new Callback<PaypalCheckoutResponse>() {
                    @Override
                    public void onResponse(Call<PaypalCheckoutResponse> call, Response<PaypalCheckoutResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String approvalUrl = response.body().approvalUrl;
                            openPaypalWebView(orderId, approvalUrl);
                        } else {
                            Toast.makeText(PaymentActivity.this, "Không thể lấy approvalUrl!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<PaypalCheckoutResponse> call, Throwable t) {
                        Toast.makeText(PaymentActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openPaypalWebView(int orderId, String approvalUrl) {
        Intent i = new Intent(this, PaypalWebViewActivity.class);
        i.putExtra("orderId", orderId);
        i.putExtra("approvalUrl", approvalUrl);
        startActivity(i);
    }
}
