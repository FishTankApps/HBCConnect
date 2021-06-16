package com.fishtankapps.hbcconnect.mobile.utilities.notification;

import android.content.Context;

import java.io.Serializable;

public abstract class NotificationClickedHandler implements Serializable {
    private static final long serialVersionUID = 6102554910102720618L;
    private final String buttonText;

    public NotificationClickedHandler(String buttonText){
        this.buttonText = buttonText;
    }

    public String getButtonText(){
        return buttonText;
    }

    public abstract void notificationClicked(Context context);
}
