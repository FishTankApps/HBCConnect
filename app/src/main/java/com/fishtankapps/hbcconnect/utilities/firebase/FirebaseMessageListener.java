package com.fishtankapps.hbcconnect.utilities.firebase;

import android.util.Log;

import com.fishtankapps.hbcconnect.activity.LivestreamViewerActivity;
import com.fishtankapps.hbcconnect.utilities.Constants;
import com.fishtankapps.hbcconnect.utilities.notification.NotificationBuilder;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FirebaseMessageListener extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FirebaseThingy", "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d("FirebaseThingy", "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d("FirebaseThingy", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        if(Objects.equals(remoteMessage.getFrom(), "/topics/live.livestream")){
            NotificationBuilder.buildNotification("New Livestream", "A new livestream just started! Click to watch!")
                    .setNotificationType("Livestreams")
                    .setNotificationTypeDescription("Receive a notification when a new livestream starts.")
                    .setOnClickActivity(LivestreamViewerActivity.class)
                    .addExtraForOnClickActivity(Constants.LIVESTREAM_ID, remoteMessage.getData().get("livestream.id"))
                    .addExtraForOnClickActivity(Constants.LIVESTREAM_NAME, "Live Livestream")
                    .sendNotification(this.getBaseContext());
        } else {
            NotificationBuilder.buildNotification("New Message:", "Topic: " + remoteMessage.getFrom() + "\n Data: " + remoteMessage.getData()).sendNotification(this);
        }
    }

    @Override
    public void onNewToken(@NotNull String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN", s);
    }
}
