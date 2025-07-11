package com.example.productsaleandroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.productsaleandroid.models.Product;

import java.util.List;

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

        // 🔥 Bắt sự kiện click mở ProductDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("name", product.getProductName());
            intent.putExtra("description", product.getFullDescription()); // dùng mô tả chi tiết hơn
            intent.putExtra("price", product.getPrice());
            intent.putExtra("image", product.getImageURL());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct, ivFavorite;
        TextView tvProductName, tvBriefDescription, tvPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBriefDescription = itemView.findViewById(R.id.tvBriefDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
