package com.example.productsaleandroid;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
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
    private float lastAngle = 0f;
    private final int SECTOR_COUNT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spin_wheel);

        imgSpinWheel = findViewById(R.id.imgSpinWheel);
        btnSpin = findViewById(R.id.btnSpin);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnSpin.setOnClickListener(v -> {
            if (isSpinning) return;
            isSpinning = true;
            btnSpin.setEnabled(false);
            spinOnce();
        });
    }

    // Quay 1 lần và xử lý kết quả (dùng cho các luồng cần show popup khi đã quay hôm nay)
    private void spinOnce() {
        startSpinAnimation(this::callSpinApiAndHandle);
    }

    // Gọi API và xử lý popup các trường hợp
    private void callSpinApiAndHandle() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        SpinApi api = ApiClient.getClient().create(SpinApi.class);
        api.spin("Bearer " + token).enqueue(new Callback<SpinResult>() {
            @Override
            public void onResponse(Call<SpinResult> call, Response<SpinResult> response) {
                isSpinning = false;
                if (response.isSuccessful() && response.body() != null) {
                    SpinResult result = response.body();

                    // Trúng voucher
                    if (result.isSuccess() && result.getData() != null && result.getData().getVoucher() != null) {
                        btnSpin.setEnabled(true);
                        showRewardDialog(result);

                        // Không trúng, còn lượt
                    } else if (result.isSuccess() && (result.getData() == null || result.getData().getVoucher() == null)) {
                        showFailDialog(true); // Cho quay tiếp sau khi đóng

                        // Hết lượt (success=false) hoặc message API trả về đã quay hôm nay
                    } else {
                        btnSpin.setEnabled(false);
                        if (result.getMessage() != null && result.getMessage().toLowerCase().contains("already spun")) {
                            showAlreadySpunDialog("Bạn đã nhận voucher ngày hôm nay.\nHãy quay lại vào hôm sau!");
                        } else {
                            showFailDialog(false); // Không cho quay tiếp
                        }
                    }

                } else {
                    // Xử lý lỗi HTTP 403 (đã quay hôm nay)
                    if (response.code() == 403 && response.errorBody() != null) {
                        try {
                            String error = response.errorBody().string();
                            Log.e("SpinWheel", "errorBody=" + error);
                            if (error.contains("already spun today")) {
                                btnSpin.setEnabled(false);
                                showAlreadySpunDialog("Bạn đã nhận voucher ngày hôm nay.\nHãy quay lại vào hôm sau!");
                                return;
                            }
                        } catch (Exception ignored) {}
                    }
                    btnSpin.setEnabled(true);
                    Toast.makeText(SpinWheelActivity.this, "Có lỗi khi kết nối máy chủ!", Toast.LENGTH_SHORT).show();
                    Log.e("SpinWheel", "Lỗi API: code=" + response.code() + " - " + response.message());
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

    // Animation xong mới gọi API (tránh gọi dồn nhiều lần)
    private void startSpinAnimation(Runnable onEnd) {
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

        rotate.setDuration(2000);
        rotate.setInterpolator(new DecelerateInterpolator());
        rotate.setFillAfter(true);

        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                lastAngle = (lastAngle + toAngle) % 360f;
                if (onEnd != null) onEnd.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        imgSpinWheel.startAnimation(rotate);
    }

    // Hiện popup trúng thưởng
    private void showRewardDialog(SpinResult result) {
        SpinResult.Voucher voucher = result.getData().getVoucher();
        new AlertDialog.Builder(this)
                .setTitle("🎉 Chúc mừng!")
                .setMessage("Bạn đã nhận được:\n🎁 Mã: " + voucher.getCode()
                        + "\nGiảm giá: " + voucher.getDiscountPercent() + "%")
                .setPositiveButton("OK", null)
                .show();
    }

    // == SHOW FAIL POPUP GIỐNG SHOPEE, truyền canContinue để biết cho phép quay tiếp hay không ==
    private void showFailDialog(boolean canContinue) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_spin_fail, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(d -> {
            if (canContinue) {
                btnSpin.setEnabled(true);
            }
        });
        dialog.show();
    }

    // == Hiện popup đã quay hôm nay, không cho quay nữa ==
    private void showAlreadySpunDialog(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .show();
    }
}
