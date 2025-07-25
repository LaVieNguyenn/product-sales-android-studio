package com.example.productsaleandroid;

import android.view.*;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.productsaleandroid.R;
import com.example.productsaleandroid.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LEFT = 0;
    private static final int TYPE_RIGHT = 1;

    private List<ChatMessage> messages;
    private int myUserId;

    public ChatAdapter(List<ChatMessage> messages, int myUserId) {
        this.messages = messages;
        this.myUserId = myUserId;
    }

    public void addMessage(ChatMessage msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    // Giữ nguyên điều kiện hiển thị trái/phải:
    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        return (msg.senderId == myUserId) ? TYPE_RIGHT : TYPE_LEFT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == TYPE_RIGHT) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, parent, false);
            return new RightViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, parent, false);
            return new LeftViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        if (getItemViewType(position) == TYPE_RIGHT) {
            ((RightViewHolder) holder).tvContent.setText(msg.content);
        } else {
            ((LeftViewHolder) holder).tvContent.setText(msg.content);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder cho tin nhắn bên trái (user khác)
    static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        LeftViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }

    // ViewHolder cho tin nhắn bên phải (của mình)
    static class RightViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        RightViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
