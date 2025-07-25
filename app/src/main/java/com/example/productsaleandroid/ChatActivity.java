package com.example.productsaleandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.productsaleandroid.api.ChatApi;
import com.example.productsaleandroid.api.SocketManager;
import com.example.productsaleandroid.models.ChatMessage;
import com.example.productsaleandroid.ChatAdapter;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import io.socket.client.Socket;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvChat;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private Socket socket;
    private int myUserId, otherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Lấy userId hiện tại và otherUserId
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        myUserId = prefs.getInt("userId", -1); // Đã lưu khi login
        Log.d("MY_USER_ID", "myUserId=" + myUserId);
        otherUserId = getIntent().getIntExtra("otherUserId", -1);

        if (myUserId == -1 || otherUserId == -1) {
            Toast.makeText(this, "Thiếu thông tin người dùng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new ChatAdapter(messages, myUserId);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // 1. Lấy lịch sử chat qua REST API
        String token = prefs.getString("token", "");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be-allora.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ChatApi chatApi = retrofit.create(ChatApi.class);

        // Giả sử getMessages có dạng: @GET /api/chat/messages?otherUserId=... @Header("Authorization")
        chatApi.getMessages("Bearer " + token, otherUserId).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    rvChat.scrollToPosition(messages.size() - 1);
                }
            }
            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Không thể tải lịch sử chat", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Kết nối Socket.IO và join phòng
        socket = SocketManager.getSocket();
        socket.connect();
        socket.emit("join", myUserId);
        socket.on(Socket.EVENT_CONNECT, args -> Log.d("SOCKET", "✅ Connected"));
        socket.on(Socket.EVENT_DISCONNECT, args -> Log.d("SOCKET", "❌ Disconnected"));
        socket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.e("SOCKET", "🚫 Connect Error: " + args[0]));
        // 3. Lắng nghe nhận tin nhắn mới (real-time)
        socket.on("receive-message", args -> {
            JSONObject obj = (JSONObject) args[0];
            Log.d("SOCKET", "📥 Raw socket: " + obj.toString());

            ChatMessage msg = new ChatMessage();
            msg.id = obj.optInt("chatMessageId");
            msg.senderId = obj.optInt("userId");
            msg.receiverId = obj.optInt("receiverId");
            msg.content = obj.optString("message");
            msg.timestamp = obj.optString("sentAt");

            Log.d("DEBUG", "SenderId = " + msg.senderId + ", ReceiverId = " + msg.receiverId + ", MyUserId = " + myUserId);

            if (msg.senderId == myUserId || msg.receiverId == myUserId) {
                runOnUiThread(() -> {
                    adapter.addMessage(msg);
                    rvChat.scrollToPosition(adapter.getItemCount() - 1);
                });
            }
        });


        // 4. Gửi tin nhắn (emit socket)
        btnSend.setOnClickListener(v -> {
            String content = etMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                JSONObject msg = new JSONObject();
                try {
                    msg.put("senderId", myUserId);
                    msg.put("receiverId", otherUserId);
                    msg.put("message", content);
                } catch (Exception e) { }
                socket.emit("send-message", msg);
                etMessage.setText("");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.off("receive-message");
            socket.disconnect();
        }
    }
}
