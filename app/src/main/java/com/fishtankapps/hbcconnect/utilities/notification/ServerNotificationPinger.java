package com.fishtankapps.hbcconnect.utilities.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;
import com.fishtankapps.hbcconnect.activity.WatchFacebookVideoActivity;
import com.fishtankapps.hbcconnect.dataStorage.DataFile;
import com.fishtankapps.hbcconnect.server.HBCConnectServer;
import com.fishtankapps.hbcconnect.utilities.Constants;

import java.io.Serializable;

public class ServerNotificationPinger extends BroadcastReceiver implements Serializable {

    private static final long serialVersionUID = 5788933743564955436L;

    public void onReceive(Context context, Intent intent) {
        Log.i("NotificationHandler", "I've been called!");

        NotificationClickedHandler handler = new NotificationClickedHandler("Test") {
            private static final long serialVersionUID = -1411526713462588067L;
            public void notificationClicked(Context context) {
                Toast.makeText(context, "YOU CLICKED ON ME!!!", Toast.LENGTH_SHORT).show();
                closeNotification(context);
            }
        };

        NotificationBuilder.buildNotification("Test", "NotificationHandler.onRecieve")
                .setNotificationClickedHandler(handler).sendNotification(context);

        new Thread(() -> pingServerForNotifications(context)).start();
    }

    private void pingServerForNotifications(Context context){
        HBCConnectServer hbcConnectServer = null;

        try {
            hbcConnectServer = new HBCConnectServer();
            boolean successful = hbcConnectServer.connect();

            if (successful) {
                int lastReceivedNotification = DataFile.getSharedPreferenceIntValue(Constants.LAST_LIVESTREAM_ID, context);

                hbcConnectServer.sendObject("new-notify." + lastReceivedNotification);
                Object message = hbcConnectServer.waitForMessage(5000);

                if (message != null) {
                    String[] newNotifications = (String[]) message;

                    for (int index = 0; index < newNotifications.length; index += 4) {
                        String notificationTitle = newNotifications[index];
                        String notificationMessage = newNotifications[index + 1];
                        String notificationType = newNotifications[index + 2];
                        int notificationID = Integer.parseInt(newNotifications[index + 3]);

                        DataFile.setSharedPreferenceIntValue(Constants.LAST_LIVESTREAM_ID, notificationID, context);

                        NotificationBuilder builder = NotificationBuilder.buildNotification(notificationTitle, notificationMessage).setNotificationType(notificationType);

                        if(notificationType.equals("Alert")) {
                            builder.setNotificationTypeDescription("Alerts such as cancellations and emergencies");
                            builder.setOnClickActivity(HBCConnectActivity.class);
                        } else if (notificationType.equals("New Livestream")) {
                            builder.setNotificationTypeDescription("When a new livestream starts");
                            builder.setOnClickActivity(WatchFacebookVideoActivity.class);
                        }

                        builder.sendNotification(context);
                    }
                }

                hbcConnectServer.disconnect();
            }
        }catch (Exception e){
            e.printStackTrace();
            if(hbcConnectServer != null)
                hbcConnectServer.disconnect();
        }
    }
}
