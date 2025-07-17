package com.example.productsaleandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.productsaleandroid.api.PaymentApi;
import com.example.productsaleandroid.models.PaypalConfirmRequest;

import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaypalWebViewActivity extends AppCompatActivity {
    private WebView webView;
    private String token;
    private int orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal_webview);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        orderId = getIntent().getIntExtra("orderId", 0);
        String approvalUrl = getIntent().getStringExtra("approvalUrl");

        if (approvalUrl == null || approvalUrl.isEmpty()) {
            Toast.makeText(this, "Không có approvalUrl!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView.setWebViewClient(new WebViewClient() {
            // For old Android
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }
            // For API 24+
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }
            private boolean handleUrl(String url) {
                if (url.startsWith("https://be-allora.onrender.com/paypal/success")) {
                    Uri uri = Uri.parse(url);
                    String paypalOrderId = uri.getQueryParameter("token");
                    if (paypalOrderId != null && !paypalOrderId.isEmpty()) {
                        confirmPaypalPayment(orderId, paypalOrderId);
                    } else {
                        Toast.makeText(PaypalWebViewActivity.this, "Không lấy được PayPal Order ID!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(approvalUrl);
    }

    private void confirmPaypalPayment(int orderId, String paypalOrderId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        PaypalConfirmRequest req = new PaypalConfirmRequest(paypalOrderId, orderId);

        paymentApi.confirmPaypal("Bearer " + token, req)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(PaypalWebViewActivity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(PaypalWebViewActivity.this, OrderConfirmationActivity.class);
                            i.putExtra("orderId", orderId);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(PaypalWebViewActivity.this, "Lỗi xác nhận thanh toán!", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(PaypalWebViewActivity.this, "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
