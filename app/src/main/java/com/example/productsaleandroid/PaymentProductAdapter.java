package com.example.productsaleandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.productsaleandroid.models.CartItem;

import java.util.List;

public class PaymentProductAdapter extends RecyclerView.Adapter<PaymentProductAdapter.PaymentProductViewHolder> {
    private List<CartItem> items;

    public PaymentProductAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PaymentProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_product, parent, false);
        return new PaymentProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentProductViewHolder holder, int position) {
        CartItem item = items.get(position);

        // Ưu tiên lấy tên và ảnh từ object product nếu có, nếu không lấy từ field trực tiếp
        String productName = item.productName;
        String imageUrl = item.imageUrl;
        if ((productName == null || productName.isEmpty()) && item.product != null) {
            productName = item.product.getProductName();
        }
        if ((imageUrl == null || imageUrl.isEmpty()) && item.product != null) {
            imageUrl = item.product.getImageURL();
        }

        holder.tvProductName.setText(productName != null ? productName : "Sản phẩm");
        holder.tvProductPrice.setText("₫" + String.format("%,.0f", item.price));
        holder.tvProductQty.setText("x" + item.quantity);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.imgProduct.getContext())
                    .load(imageUrl)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class PaymentProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvProductQty;

        public PaymentProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQty = itemView.findViewById(R.id.tvProductQty);
        }
    }
}
