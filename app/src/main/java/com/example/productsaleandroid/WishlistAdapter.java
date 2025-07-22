package com.example.productsaleandroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.productsaleandroid.api.ApiClient;
import com.example.productsaleandroid.api.WishlistApi;
import com.example.productsaleandroid.models.Product;
import com.example.productsaleandroid.models.WishlistItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {
    private List<WishlistItem> items;
    private Context context;

    public WishlistAdapter(List<WishlistItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WishlistItem item = items.get(position);

        holder.name.setText(item.getProduct().getProductName());
        holder.desc.setText(item.getProduct().getBriefDescription());
        holder.price.setText(item.getProduct().getPrice() + "đ");

        Glide.with(context).load(item.getProduct().getImageURL()).into(holder.image);

        holder.heart.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            WishlistItem currentItem = items.get(currentPosition);
            String token = context.getSharedPreferences("app", Context.MODE_PRIVATE).getString("token", "");

            ApiClient.getClient().create(WishlistApi.class)
                    .removeFromWishlist("Bearer " + token, currentItem.getProductId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            items.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        holder.itemView.setOnClickListener(v -> {
            Product product = item.getProduct();  // ✅ Lấy object product bên trong WishlistItem

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
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, desc, price;
        ImageButton heart;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgProduct);
            name = itemView.findViewById(R.id.tvName);
            desc = itemView.findViewById(R.id.tvDesc);
            price = itemView.findViewById(R.id.tvPrice);
            heart = itemView.findViewById(R.id.btnHeart);
        }
    }
}

