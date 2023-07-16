package com.akapps.dailynote.classes.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlertDismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", -1);

        if(notificationId != -1) {
            // cancel notification
            Helper.cancelNotification(context, notificationId);
        }
    }
}

