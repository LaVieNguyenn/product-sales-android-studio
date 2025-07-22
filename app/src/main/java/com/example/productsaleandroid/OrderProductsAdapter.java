package com.example.productsaleandroid;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.productsaleandroid.models.CartItem;
import com.example.productsaleandroid.models.OrderCartItem;
import java.util.List;
public class OrderProductsAdapter extends RecyclerView.Adapter<OrderProductsAdapter.ProductViewHolder> {
    private List<CartItem> items;

    public OrderProductsAdapter(List<CartItem> items) {
        this.items = items;
    }
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_product, parent, false);
        return new ProductViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.tvProductName.setText(item.product != null ? item.product.getProductName() : "Sản phẩm");
        holder.tvProductPrice.setText("₫" + String.format("%,.0f", item.price));
        holder.tvProductQty.setText("x" + item.quantity);
        if (item.product != null && item.product.getImageURL() != null) {
            Glide.with(holder.imgProduct.getContext())
                    .load(item.product.getImageURL())
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo);
        }
    }
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvProductQty;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQty = itemView.findViewById(R.id.tvProductQty);
        }
    }
}
