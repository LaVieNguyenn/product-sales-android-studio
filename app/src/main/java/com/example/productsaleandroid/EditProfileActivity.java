package com.example.productsaleandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.UserApi;
import com.example.productsaleandroid.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private EditText edtUsername, edtEmail, edtPhone, edtAddress;
    private TextView tvSave;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        tvSave = findViewById(R.id.tvSave);
        btnBack = findViewById(R.id.btnBack);

        // TODO: Lấy user từ Intent hoặc gọi lại API getMe và fill dữ liệu
        // Ví dụ:
        // User user = ...;
        // edtUsername.setText(user.getUsername());
        // ...

        btnBack.setOnClickListener(v -> finish());

        tvSave.setOnClickListener(v -> {
            String username = edtUsername.getText().toString();
            String email = edtEmail.getText().toString();
            String phone = edtPhone.getText().toString();
            String address = edtAddress.getText().toString();
            updateProfile(username, email, phone, address);
        });
    }

    private void updateProfile(String username, String email, String phoneNumber, String address) {
        // Lấy token user
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        if (token.isEmpty()) return;

        // Tạo request
        User userUpdate = new User(username, email, phoneNumber, address);

        UserApi userApi = ApiClient.getClient().create(UserApi.class);

        userApi.updateMe("Bearer " + token, userUpdate).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

