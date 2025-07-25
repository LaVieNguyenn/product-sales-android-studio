package com.example.productsaleandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.productsaleandroid.api.AuthService;
import com.example.productsaleandroid.models.UserRegisterRequest;
import com.example.productsaleandroid.models.UserRegisterResponse;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword, edtUsername, edtPhone, edtAddress;
    Button btnRegister;
    TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || username.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please enter all information!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo retrofit client & request
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AuthService authService = retrofit.create(AuthService.class);

        UserRegisterRequest req = new UserRegisterRequest(username, pass, email, phone, address);

        btnRegister.setEnabled(false);

        authService.register(req).enqueue(new Callback<UserRegisterResponse>() {
            @Override
            public void onResponse(Call<UserRegisterResponse> call, Response<UserRegisterResponse> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Hãy kiểm tra email để xác thực.", Toast.LENGTH_LONG).show();
                    showVerifyDialog();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại. Email đã tồn tại!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserRegisterResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị dialog nhập mã xác thực
    private void showVerifyDialog() {
        EditText edtToken = new EditText(this);
        edtToken.setHint("Nhập mã xác thực email (token)");
        edtToken.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this)
                .setTitle("Xác thực Email")
                .setMessage("Vui lòng mở email và nhập mã token xác thực nhận được")
                .setView(edtToken)
                .setPositiveButton("Xác thực", (dialog, which) -> {
                    String token = edtToken.getText().toString().trim();
                    if (!token.isEmpty()) verifyEmail(token);
                })
                .setNegativeButton("Bỏ qua", null)
                .show();
    }

    // Gọi API xác thực email
    private void verifyEmail(String token) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AuthService authService = retrofit.create(AuthService.class);

        authService.verifyEmail(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Xác thực thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Mã xác thực không đúng!", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
