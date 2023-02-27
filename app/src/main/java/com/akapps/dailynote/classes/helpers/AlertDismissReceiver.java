package com.akapps.dailynote.classes.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlertDismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", -1);

        if(notificationId != -1) {
            // notification id uses note id, so it is used to find note and remove its reminder
            RealmHelper.updateNoteReminder(context, notificationId);

            // cancel notification
            Helper.cancelNotification(context, notificationId);
        }
    }
}

