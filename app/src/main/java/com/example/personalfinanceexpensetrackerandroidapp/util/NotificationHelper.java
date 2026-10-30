package com.example.personalfinanceexpensetrackerandroidapp.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.ui.management.SmsReviewActivity;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    public static final String CHANNEL_ID_SMS = "CHANNEL_ID_SMS";
    public static final String CHANNEL_NAME_SMS = "Transaction SMS Notifications";
    public static final String NOTIFICATION_TITLE = "MoneyFlow - New SMS Transaction";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_SMS,
                    CHANNEL_NAME_SMS,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for incoming transaction SMS from whitelisted senders");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void showSmsNotification(Context context, String sender, double amount, String transactionType) {
        String action;
        if ("IN".equalsIgnoreCase(transactionType)) {
            action = "Received ";
        } else if ("TRANSFER".equalsIgnoreCase(transactionType)) {
            action = "Transferred ";
        } else {
            action = "Paid ";
        }

        String formattedAmount = CurrencyUtils.format(context, amount);
        String displaySender = (sender != null && !sender.trim().isEmpty()) ? sender : "SMS";
        String contentText = action + formattedAmount + " via " + displaySender;

        showSmsNotification(context, contentText);
    }

    @SuppressLint({"MissingPermission", "NotificationPermission"})
    public static void showSmsNotification(Context context, String contentText) {
        createNotificationChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Cannot post notification.");
                return;
            }
        }

        Intent intent = new Intent(context, SmsReviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                flags
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_SMS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            int notificationId = (int) (System.currentTimeMillis() & 0x7fffffff);
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification displayed: " + contentText);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException while showing notification", e);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }
}
