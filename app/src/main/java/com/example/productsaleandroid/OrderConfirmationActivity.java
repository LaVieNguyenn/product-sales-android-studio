package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.productsaleandroid.api.OrderApi;
import com.example.productsaleandroid.models.*;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrderConfirmationActivity extends AppCompatActivity {
    private TextView tvOrderStatus, tvOrderId, tvOrderDate, tvPaymentMethod, tvBillingAddress, tvTotal;
    private RecyclerView rvProducts;
    private Button btnBackHome;
    private String token;
    private OrderProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvBillingAddress = findViewById(R.id.tvBillingAddress);
        tvTotal = findViewById(R.id.tvTotal);
        rvProducts = findViewById(R.id.rvProducts);
        btnBackHome = findViewById(R.id.btnBackHome);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        int orderId = getIntent().getIntExtra("orderId", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không có mã đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchOrderDetail(orderId);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchOrderDetail(int orderId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OrderApi orderApi = retrofit.create(OrderApi.class);
        orderApi.getOrderById("Bearer " + token, orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showOrderDetail(response.body());
                } else {
                    Toast.makeText(OrderConfirmationActivity.this, "Không tìm thấy đơn hàng!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                Toast.makeText(OrderConfirmationActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showOrderDetail(OrderDetailResponse order) {
        // Xử lý trạng thái hiển thị theo orderStatus
        String status = order.orderStatus != null ? order.orderStatus : "N/A";
        if ("paid".equalsIgnoreCase(status)) {
            tvOrderStatus.setText("Đơn hàng đã thanh toán");
        } else if ("completed".equalsIgnoreCase(status)) {
            tvOrderStatus.setText("Đơn hàng đã hoàn thành");
        } else if ("processing".equalsIgnoreCase(status)) {
            tvOrderStatus.setText("Đơn hàng đang xử lý");
        } else {
            tvOrderStatus.setText("Trạng thái: " + status);
        }

        tvOrderId.setText("Mã đơn: #" + order.orderId);

        // Format lại ngày giờ cho dễ nhìn
        String dateStr = order.orderDate != null ? order.orderDate.replace("T", " ").replace("Z", "") : "N/A";
        tvOrderDate.setText("Ngày đặt: " + dateStr);

        tvPaymentMethod.setText("Thanh toán: " + (order.paymentMethod != null ? order.paymentMethod : "N/A"));
        tvBillingAddress.setText("Địa chỉ: " + (order.billingAddress != null ? order.billingAddress : "N/A"));

        // Danh sách sản phẩm
        if (order.cart != null && order.cart.cartItems != null) {
            adapter = new OrderProductAdapter(order.cart.cartItems);
            rvProducts.setAdapter(adapter);

            // Hiển thị finalPrice đã trừ voucher nếu có
            if (order.cart.finalPrice > 0) {
                tvTotal.setText("Tổng cộng: ₫" + String.format("%,.0f", order.cart.finalPrice));
            } else {
                double total = 0;
                for (CartItem item : order.cart.cartItems) {
                    total += item.price * item.quantity;
                }
                tvTotal.setText("Tổng cộng: ₫" + String.format("%,.0f", total));
            }
        } else {
            tvTotal.setText("Tổng cộng: ₫0");
        }
    }
}
