package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.OrderApi;
import com.example.productsaleandroid.models.OrderSummary;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerOrders;
    private OrderListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        loadOrders();
    }

    private void loadOrders() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        ApiClient.getClient().create(OrderApi.class)
                .getMyOrders("Bearer " + token)
                .enqueue(new Callback<List<OrderSummary>>() {
                    @Override
                    public void onResponse(Call<List<OrderSummary>> call, Response<List<OrderSummary>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<OrderSummary> orders = response.body();
                            adapter = new OrderListAdapter(orders, OrderHistoryActivity.this);
                            recyclerOrders.setAdapter(adapter);
                        } else {
                            Toast.makeText(OrderHistoryActivity.this, "Không lấy được đơn mua!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<OrderSummary>> call, Throwable t) {
                        Toast.makeText(OrderHistoryActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
