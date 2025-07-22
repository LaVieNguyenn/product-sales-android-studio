package com.example.productsaleandroid;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.SpinApi;
import com.example.productsaleandroid.models.SpinResult;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpinWheelActivity extends AppCompatActivity {

    private ImageView imgSpinWheel;
    private Button btnSpin;
    private boolean isSpinning = false;
    private float lastAngle = 0f; // để tiếp nối mượt
    private final int SECTOR_COUNT = 6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spin_wheel);

        imgSpinWheel = findViewById(R.id.imgSpinWheel);
        btnSpin = findViewById(R.id.btnSpin);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });
        btnSpin.setOnClickListener(v -> {
            if (isSpinning) return;
            isSpinning = true;
            btnSpin.setEnabled(false);
            startSpinAnimation();
        });
    }

    private void startSpinAnimation() {
        int targetIndex = new Random().nextInt(SECTOR_COUNT); // [0-5]
        int anglePerSector = 360 / SECTOR_COUNT;
        int targetAngle = targetIndex * anglePerSector;

        int fullRotations = 6;
        float toAngle = fullRotations * 360f + targetAngle;

        RotateAnimation rotate = new RotateAnimation(
                lastAngle,
                lastAngle + toAngle,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        rotate.setDuration(4000); // quay mượt hơn
        rotate.setInterpolator(new DecelerateInterpolator());
        rotate.setFillAfter(true);

        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                lastAngle = (lastAngle + toAngle) % 360f;
                callSpinApi();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        imgSpinWheel.startAnimation(rotate);
    }

    private void callSpinApi() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        SpinApi api = ApiClient.getClient().create(SpinApi.class);
        api.spin("Bearer " + token).enqueue(new Callback<SpinResult>() {
            @Override
            public void onResponse(Call<SpinResult> call, Response<SpinResult> response) {
                isSpinning = false;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showRewardDialog(response.body());
                } else {
                    btnSpin.setEnabled(false);
                    Toast.makeText(SpinWheelActivity.this, "Bạn đã quay hôm nay. Vui lòng quay lại ngày mai!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SpinResult> call, Throwable t) {
                isSpinning = false;
                btnSpin.setEnabled(true);
                Toast.makeText(SpinWheelActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRewardDialog(SpinResult result) {
        SpinResult.Voucher voucher = result.getData().getVoucher();
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã nhận được:\n🎁 Mã: " + voucher.getCode()
                        + "\nGiảm giá: " + voucher.getDiscountPercent() + "%")
                .setPositiveButton("OK", null)
                .show();
    }
}
