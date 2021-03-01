package com.fishtankapps.hbcconnect.utilities.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;
import com.fishtankapps.hbcconnect.utilities.Constants;

public class NotificationBuilder {

    private static int currentNotificationID = 0;
    private static int counter = 0;

    private String notificationTypeDescription;
    private String notificationType;
    private final String message;
    private final String title;

    private int importance;

    private NotificationClickedHandler notificationClickedHandler;
    private Class<?> onClickActivity;

    private NotificationBuilder(String title, String message){
        this.title = title;
        this.message = message;

        notificationType = "General Notification";
        notificationTypeDescription = "";

        onClickActivity = HBCConnectActivity.class;
        notificationClickedHandler = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            importance = NotificationManager.IMPORTANCE_DEFAULT;
    }

    public NotificationBuilder setImportance(int importance){
        this.importance = importance;
        return this;
    }
    public NotificationBuilder setNotificationType(String notificationType){
        this.notificationType = notificationType;
        return this;
    }
    public NotificationBuilder setNotificationTypeDescription(String notificationTypeDescription){
        this.notificationTypeDescription = notificationTypeDescription;
        return this;
    }
    public NotificationBuilder setNotificationClickedHandler(NotificationClickedHandler notificationClickedHandler){
        this.notificationClickedHandler = notificationClickedHandler;
        return this;
    }
    public NotificationBuilder setOnClickActivity(Class<?> onClickActivity){
        this.onClickActivity = onClickActivity;
        return this;
    }

    public void sendNotification(Context context) {
        Intent intent = new Intent(context, onClickActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, counter++, intent, 0);

        int notificationID = currentNotificationID++;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "HBC_Connect_Notification")
                .setSmallIcon(R.mipmap.hbc_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if(notificationClickedHandler != null && !notificationClickedHandler.getButtonText().equals("")){
            notificationClickedHandler.setNotificationID(notificationID);

            Intent onPressIntent = new Intent(context, NotificationClickedReceiver.class);
            onPressIntent.putExtra(Constants.NOTIFICATION_CLICKED_HANDLER, notificationClickedHandler);
            PendingIntent onPressPendingIntent = PendingIntent.getBroadcast(context, counter++, onPressIntent, 0);

            builder.addAction(R.mipmap.hbc_logo, notificationClickedHandler.getButtonText(), onPressPendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("HBC_Connect_Notification", notificationType, importance);
            channel.setDescription(notificationTypeDescription);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationID, builder.build());
    }

    public static NotificationBuilder buildNotification(String title, String message){
        return new NotificationBuilder(title, message);
    }
}
