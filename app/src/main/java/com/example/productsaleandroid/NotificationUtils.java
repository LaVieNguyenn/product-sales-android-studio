package com.example.productsaleandroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {
    private static final String CHANNEL_ID = "cart_channel";
    private static final int NOTIF_ID = 1001;

    // Hiện thông báo badge số lượng cart
    public static void showCartBadgeNotification(Context context, int cartCount) {
        // Chỉ kiểm tra quyền cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Không có quyền thì không gửi (nên xin quyền trong Activity)
                return;
            }
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel cho Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Cart Notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo Intent mở CartActivity
        Intent intent = new Intent(context, CartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cart) // icon tuỳ bạn
                .setContentTitle("Giỏ hàng")
                .setContentText("Bạn có " + cartCount + " sản phẩm trong giỏ hàng!")
                .setAutoCancel(true)
                .setNumber(cartCount) // Gợi ý hiển thị badge
                .setContentIntent(pendingIntent); // <- Sẽ mở CartActivity khi nhấn

        notificationManager.notify(NOTIF_ID, builder.build());
    }

    // Hủy notification khi giỏ hàng trống
    public static void cancelCartBadge(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIF_ID);
    }
}
