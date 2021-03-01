package com.fishtankapps.hbcconnect.utilities.notification;

import android.app.NotificationManager;
import android.content.Context;

import java.io.Serializable;

public abstract class NotificationClickedHandler implements Serializable {
    private static final long serialVersionUID = 6102554910102720618L;
    private final String buttonText;
    private int notificationID;

    public NotificationClickedHandler(String buttonText){
        this.buttonText = buttonText;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public String getButtonText(){
        return buttonText;
    }

    public void closeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationID);
    }

    public abstract void notificationClicked(Context context);
}
