package com.example.productsaleandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.WishlistApi;
import com.example.productsaleandroid.models.Product;
import com.example.productsaleandroid.models.WishlistRequest;
import com.example.productsaleandroid.models.WishlistResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) return;

        holder.tvProductName.setText(product.getProductName());
        holder.tvBriefDescription.setText(product.getBriefDescription() != null ? product.getBriefDescription() : "Không có mô tả");
        String formattedPrice = String.format("%,.0f VND", product.getPrice());
        holder.tvPrice.setText(formattedPrice);

        Glide.with(context)
                .load(product.getImageURL())
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.imgProduct);

        holder.ivFavorite.setOnClickListener(v -> {
            addToWishlist(product.getProductId());
        });

        // Sự kiện click vào sản phẩm
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", product.getProductId());
            intent.putExtra("name", product.getProductName());
            intent.putExtra("technicalSpecification", product.getTechnicalSpecifications());
            intent.putExtra("description", product.getFullDescription());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("image", product.getImageURL());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    private void addToWishlist(int productId) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(context, "Vui lòng đăng nhập để thêm vào yêu thích.", Toast.LENGTH_SHORT).show();
            return;
        }

        WishlistApi wishlistApi = ApiClient.getClient().create(WishlistApi.class);
        WishlistRequest request = new WishlistRequest(productId);
        wishlistApi.addToWishlist("Bearer " + token, request).enqueue(new Callback<WishlistResponse>() {
            @Override
            public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WishlistResponse> call, Throwable t) {
                Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("WISHLIST", "Lỗi: " + t.getMessage());
            }
        });
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct, ivFavorite;
        TextView tvProductName, tvBriefDescription, tvPrice, tvTechnicalSpecification;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvTechnicalSpecification = itemView.findViewById(R.id.tvTechnicalSpecification);
            tvBriefDescription = itemView.findViewById(R.id.tvBriefDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
