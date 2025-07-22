package com.example.productsaleandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.OrderApi;
import com.example.productsaleandroid.models.CartItem;
import com.example.productsaleandroid.models.OrderDetail;
import com.example.productsaleandroid.models.OrderCartItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView tvOrderStatus, tvOrderInfo, tvTotalPrice;
    private RecyclerView recyclerOrderProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderInfo = findViewById(R.id.tvOrderInfo);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        recyclerOrderProducts = findViewById(R.id.recyclerOrderProducts);

        int orderId = getIntent().getIntExtra("orderId", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không có mã đơn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadOrderDetail(orderId);
    }

    private void loadOrderDetail(int orderId) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        ApiClient.getClient().create(OrderApi.class)
                .getOrderByIdDetail("Bearer " + token, orderId)
                .enqueue(new Callback<OrderDetail>() {
                    @Override
                    public void onResponse(Call<OrderDetail> call, Response<OrderDetail> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            OrderDetail order = response.body();
                            tvOrderStatus.setText("paid".equals(order.orderStatus) ? "Hoàn thành" : "Đang xử lý");
                            tvOrderInfo.setText("Phương thức: " + order.paymentMethod
                                    + " | Địa chỉ: " + order.billingAddress);
                            tvTotalPrice.setText("Tổng cộng: ₫" + String.format("%,d", order.cart.totalPrice));
                            List<CartItem> items = order.cart.items;

                            recyclerOrderProducts.setLayoutManager(new LinearLayoutManager(OrderDetailActivity.this));
                            recyclerOrderProducts.setAdapter(new OrderProductsAdapter(items));
                        } else {
                            Toast.makeText(OrderDetailActivity.this, "Không lấy được chi tiết đơn hàng!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override
                    public void onFailure(Call<OrderDetail> call, Throwable t) {
                        Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
