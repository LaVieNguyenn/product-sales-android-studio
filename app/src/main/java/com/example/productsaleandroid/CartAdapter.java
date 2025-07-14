
package com.example.productsaleandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.productsaleandroid.api.CartApi;
import com.example.productsaleandroid.api.UpdateQuantityRequest;
import com.example.productsaleandroid.models.CartItem;
import com.example.productsaleandroid.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> items;
    private OnItemCheckedChangeListener onItemCheckedChangeListener;
    private String token;

    // Thêm biến edit mode
    private boolean isEditMode = false;

    public CartAdapter(Context context, List<CartItem> items, String token) {
        this.context = context;
        this.items = items;
        this.token = token;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        notifyDataSetChanged();
        if (onItemCheckedChangeListener != null) {
            onItemCheckedChangeListener.onCheckedChanged();
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    // Hàm chuyển trạng thái edit mode
    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public interface OnItemCheckedChangeListener {
        void onCheckedChanged();
    }

    public void setOnItemCheckedChangeListener(OnItemCheckedChangeListener listener) {
        this.onItemCheckedChangeListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        Product product = item.product;

        holder.tvProductName.setText(product != null ? product.getProductName() : "Sản phẩm");
        holder.tvProductPrice.setText(String.format("%,.0f đ", item.price));
        holder.tvQuantity.setText(String.valueOf(item.quantity));

        if (product != null && product.getImageURL() != null && !product.getImageURL().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageURL())
                    .placeholder(R.drawable.logo)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo);
        }

        // Checkbox chọn/xóa
        holder.cbItemSelect.setOnCheckedChangeListener(null);
        holder.cbItemSelect.setChecked(item.isSelected);
        holder.cbItemSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.isSelected = isChecked;
            if (onItemCheckedChangeListener != null) {
                onItemCheckedChangeListener.onCheckedChanged();
            }
        });

        // Nút trừ: disable khi <= 0
        holder.btnMinus.setEnabled(item.quantity > 0);

        holder.btnMinus.setOnClickListener(v -> {
            int newQuantity = item.quantity - 1;
            if (newQuantity < 1) {
                showConfirmRemoveDialog(item, holder.getAdapterPosition());
            } else {
                updateQuantityApi(item.cartItemID, newQuantity, item, holder);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            updateQuantityApi(item.cartItemID, item.quantity + 1, item, holder);
        });

        holder.cbItemSelect.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // Gọi API cập nhật số lượng
    private void updateQuantityApi(int cartItemId, int newQuantity, CartItem item, CartViewHolder holder) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartApi cartApi = retrofit.create(CartApi.class);
        UpdateQuantityRequest req = new UpdateQuantityRequest(newQuantity);

        cartApi.updateCartItemQuantity("Bearer " + token, cartItemId, req)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            item.quantity = newQuantity;
                            notifyItemChanged(holder.getAdapterPosition());
                            if (onItemCheckedChangeListener != null)
                                onItemCheckedChangeListener.onCheckedChanged();
                        } else {
                            String errMsg = "Lỗi cập nhật số lượng!";
                            try {
                                if (response.errorBody() != null) {
                                    errMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {}
                            android.util.Log.e("API_UPDATE_QTY", "Update fail code: " + response.code() + " | " + errMsg);
                            Toast.makeText(context, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        android.util.Log.e("API_UPDATE_QTY", "Retrofit fail: " + t.getMessage(), t);
                        Toast.makeText(context, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hiện dialog xác nhận xóa
    private void showConfirmRemoveDialog(CartItem item, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setMessage("Bạn chắc chắn muốn bỏ sản phẩm này?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    removeCartItemApi(item, position);
                })
                .show();
    }

    // Xóa sản phẩm khỏi giỏ (API)
    private void removeCartItemApi(CartItem item, int position) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CartApi cartApi = retrofit.create(CartApi.class);
        cartApi.deleteCartItem("Bearer " + token, item.cartItemID)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            items.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Đã bỏ sản phẩm khỏi giỏ!", Toast.LENGTH_SHORT).show();
                            if (onItemCheckedChangeListener != null)
                                onItemCheckedChangeListener.onCheckedChanged();
                        } else {
                            Toast.makeText(context, "Lỗi xóa sản phẩm!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(context, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnMinus, btnPlus;
        TextView tvProductName, tvProductPrice, tvQuantity;
        CheckBox cbItemSelect;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            cbItemSelect = itemView.findViewById(R.id.cbItemSelect);

            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
