package com.akapps.dailynote.classes.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra("id", -1);
        int allNoteSize = intent.getIntExtra("size", -1);
        int notePinNumber = intent.getIntExtra("pin", -1);
        String noteTitle = intent.getStringExtra("title");
        boolean fingerprint = intent.getBooleanExtra("fingerprint", false);
        boolean isChecklist = intent.getBooleanExtra("checklist", false);

        NotificationHelper notificationHelper = new NotificationHelper(context, noteId,
                notePinNumber, noteTitle, fingerprint, isChecklist, allNoteSize);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(noteId, nb.build());
    }
}

