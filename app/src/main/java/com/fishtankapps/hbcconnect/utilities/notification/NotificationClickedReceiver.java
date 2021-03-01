package com.fishtankapps.hbcconnect.utilities.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.fishtankapps.hbcconnect.utilities.Constants;

public class NotificationClickedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            NotificationClickedHandler handler = (NotificationClickedHandler) intent.getSerializableExtra(Constants.NOTIFICATION_CLICKED_HANDLER);
            handler.notificationClicked(context);
        } else {
            Log.e("NotificationClicked", "onReceive() - Bundle == null!");
        }

    }
}
