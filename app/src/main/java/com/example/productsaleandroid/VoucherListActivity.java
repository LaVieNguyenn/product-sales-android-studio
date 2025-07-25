package com.example.productsaleandroid;

import android.content.*;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.productsaleandroid.api.VoucherApi;
import com.example.productsaleandroid.models.UserVoucher;
import java.util.*;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class VoucherListActivity extends AppCompatActivity {
    RecyclerView rvVouchers;
    VoucherAdapter adapter;
    List<UserVoucher> voucherList = new ArrayList<>();
    String token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_list);

        rvVouchers = findViewById(R.id.rvVouchers);
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));

        // Lấy token từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        // Gọi API lấy voucher
        fetchVouchers();
    }

    private void fetchVouchers() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VoucherApi api = retrofit.create(VoucherApi.class);
        api.getMyVouchers("Bearer " + token).enqueue(new Callback<VoucherListResponse>() {
            @Override
            public void onResponse(Call<VoucherListResponse> call, Response<VoucherListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().vouchers != null) {
                    // LỌC CHỈ VOUCHER CHƯA REDEEMED
                    List<UserVoucher> availableVouchers = new ArrayList<>();
                    for (UserVoucher v : response.body().vouchers) {
                        // Kiểm tra trường redeemed của UserVoucher (kiểu boolean)
                        if (v.redeemed == false) {
                            availableVouchers.add(v);
                        }
                    }
                    voucherList = availableVouchers;

                    adapter = new VoucherAdapter(VoucherListActivity.this, voucherList, userVoucher -> {
                        // Trả về userVoucherId khi chọn
                        Intent result = new Intent();
                        result.putExtra("userVoucherId", userVoucher.userVoucherId);
                        result.putExtra("voucherCode", userVoucher.voucher.code);
                        setResult(RESULT_OK, result);
                        finish();
                    });
                    rvVouchers.setAdapter(adapter);
                } else {
                    Toast.makeText(VoucherListActivity.this, "Không lấy được voucher", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<VoucherListResponse> call, Throwable t) {
                Toast.makeText(VoucherListActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Model response cho API getMyVouchers
    public static class VoucherListResponse {
        public boolean success;
        public List<UserVoucher> vouchers;
    }
}
