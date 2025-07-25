package com.example.productsaleandroid.models;


import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("chatMessageId")
    public int id;

    @SerializedName("userId")
    public int senderId;

    @SerializedName("receiverId")
    public int receiverId;

    @SerializedName("message")
    public String content;

    @SerializedName("sentAt")
    public String timestamp;
}
