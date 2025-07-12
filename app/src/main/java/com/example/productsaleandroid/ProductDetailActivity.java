package com.example.productsaleandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProductDetailActivity extends AppCompatActivity {

    ImageView imgProductDetail;
    TextView tvProductNameDetail, tvProductPriceDetail, tvProductDescriptionDetail, tvTechnicalSpecification;
    ImageView btnBack; // Thêm biến nút back

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        imgProductDetail = findViewById(R.id.imgProductDetail);
        tvProductNameDetail = findViewById(R.id.tvProductNameDetail);
        tvTechnicalSpecification = findViewById(R.id.tvTechnicalSpecification);
        tvProductPriceDetail = findViewById(R.id.tvProductPriceDetail);
        tvProductDescriptionDetail = findViewById(R.id.tvProductDescriptionDetail);
        btnBack = findViewById(R.id.btnBack); // Gán nút back

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String technicalSpecification = intent.getStringExtra("technicalSpecification");
        String description = intent.getStringExtra("description");
        double price = intent.getDoubleExtra("price", 0);
        String imageUrl = intent.getStringExtra("image");

        // Hiển thị dữ liệu
        tvProductNameDetail.setText(name);
        tvProductDescriptionDetail.setText(description);
        tvTechnicalSpecification.setText((technicalSpecification));
        tvProductPriceDetail.setText(String.format("₫%,.0f VND", price));
        TextView tvVoucherPrice = findViewById(R.id.tvVoucherPrice);
        tvVoucherPrice.setText(String.format("₫%,.0f", price));
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(imgProductDetail);
    }
}
