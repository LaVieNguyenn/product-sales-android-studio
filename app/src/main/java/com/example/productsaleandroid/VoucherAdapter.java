package com.example.productsaleandroid;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.productsaleandroid.models.UserVoucher;
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {
    private final List<UserVoucher> voucherList;
    private final Context context;
    private final OnApplyClickListener listener;

    public interface OnApplyClickListener {
        void onApply(UserVoucher userVoucher);
    }

    public VoucherAdapter(Context context, List<UserVoucher> list, OnApplyClickListener listener) {
        this.context = context;
        this.voucherList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        UserVoucher uv = voucherList.get(position);
        holder.tvVoucherCode.setText("Mã: " + uv.voucher.code);
        holder.tvDiscount.setText("Giảm giá: " + uv.voucher.discountPercent + "%");
        holder.tvExpiry.setText("HSD: " + uv.voucher.expiryDate.substring(0,10));
        holder.btnApply.setOnClickListener(v -> listener.onApply(uv));
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvVoucherCode, tvDiscount, tvExpiry;
        Button btnApply;
        VoucherViewHolder(View itemView) {
            super(itemView);
            tvVoucherCode = itemView.findViewById(R.id.tvVoucherCode);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            btnApply = itemView.findViewById(R.id.btnApply);
        }
    }
}
