package com.example.productsaleandroid.api;
import io.socket.client.IO;
import io.socket.client.Socket;
public class SocketManager {
    private static Socket socket;
    public static Socket getSocket() {
        if (socket == null) {
            try {
                socket = IO.socket("https://be-allora.onrender.com");
            } catch (Exception e) { e.printStackTrace(); }
        }
        return socket;
    }
}
