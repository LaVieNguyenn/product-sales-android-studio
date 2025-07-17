package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.productsaleandroid.api.OrderApi;
import com.example.productsaleandroid.api.PaymentApi;
import com.example.productsaleandroid.api.UserApi;
import com.example.productsaleandroid.models.*;

import java.util.ArrayList;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentActivity extends AppCompatActivity {
    private TextView tvReceiverName, tvReceiverAddress, tvTotal;
    private Button btnPlaceOrder;
    private RecyclerView rvSelectedProducts;
    private String token;
    private String paymentMethod = "Paypal";
    private String billingAddress = "";
    private double totalAmount = 0;
    private ArrayList<CartItem> selectedItems;
    private PaymentProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverAddress = findViewById(R.id.tvReceiverAddress);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        rvSelectedProducts = findViewById(R.id.rvSelectedProducts);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Lấy token từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        // GỌI API LẤY TÊN VÀ ĐỊA CHỈ
        fetchUserInfo();

        // Nhận list sản phẩm từ Intent
        Intent intent = getIntent();
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
        for (CartItem item : selectedItems) {
            totalAmount += item.price * item.quantity;
        }
        tvTotal.setText("₫" + String.format("%,.0f", totalAmount));

        btnPlaceOrder.setOnClickListener(v -> createOrderAndCheckout());
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
        OrderRequest order = new OrderRequest(paymentMethod, billingAddress);

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
