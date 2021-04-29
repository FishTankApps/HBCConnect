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
import com.fishtankapps.hbcconnect.dataStorage.DataFile;
import com.fishtankapps.hbcconnect.utilities.Constants;

import java.io.Serializable;
import java.util.ArrayList;

public class NotificationBuilder {

    private static int currentNotificationID = 0;

    private String notificationTypeDescription;
    private String notificationType;
    private final String message;
    private final String title;

    private final ArrayList<String> extrasKeys;
    private final ArrayList<Serializable> extrasObjects;

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

        extrasKeys = new ArrayList<>();
        extrasObjects = new ArrayList<>();

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

    public NotificationBuilder addExtraForOnClickActivity(String key, Serializable object){
        extrasKeys.add(key);
        extrasObjects.add(object);

        return this;
    }

    public void sendNotification(Context context) {
        Intent intent = new Intent(context, onClickActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        for(int index = 0; index < extrasKeys.size(); index++){
            intent.putExtra(extrasKeys.get(index), extrasObjects.get(index));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, DataFile.getSharedPreferenceIntValue(Constants.NOTIFICATION_REQUEST_CODE, context), intent, 0);

        int notificationID = currentNotificationID++;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "HBC_Connect_Notification")
                .setSmallIcon(R.mipmap.hbc_logo_plumb)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if(notificationClickedHandler != null && !notificationClickedHandler.getButtonText().equals("")){
            notificationClickedHandler.setNotificationID(notificationID);

            Intent onPressIntent = new Intent(context, NotificationClickedReceiver.class);
            onPressIntent.putExtra(Constants.NOTIFICATION_CLICKED_HANDLER, notificationClickedHandler);
            PendingIntent onPressPendingIntent = PendingIntent.getBroadcast(context, DataFile.getSharedPreferenceIntValue(Constants.NOTIFICATION_REQUEST_CODE, context), onPressIntent, 0);

            builder.addAction(R.mipmap.hbc_logo_plumb, notificationClickedHandler.getButtonText(), onPressPendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("HBC_Connect_Notification", notificationType, importance);
            channel.setDescription(notificationTypeDescription);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationID, builder.build());

        DataFile.setSharedPreferenceIntValue(Constants.NOTIFICATION_REQUEST_CODE,
                DataFile.getSharedPreferenceIntValue(Constants.NOTIFICATION_REQUEST_CODE, context) + 2, context);
    }

    public static NotificationBuilder buildNotification(String title, String message){
        return new NotificationBuilder(title, message);
    }
}
