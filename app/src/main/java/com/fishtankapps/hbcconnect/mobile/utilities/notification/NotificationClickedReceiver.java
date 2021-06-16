package com.fishtankapps.hbcconnect.mobile.utilities.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.fishtankapps.hbcconnect.R;

public class NotificationClickedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            ((NotificationClickedHandler) intent.getSerializableExtra(context.getString(R.string.notification_clicked_handler))).notificationClicked(context);
        } else {
            Log.e("NotificationClicked", "onReceive() - Bundle == null!");
        }

    }
}
