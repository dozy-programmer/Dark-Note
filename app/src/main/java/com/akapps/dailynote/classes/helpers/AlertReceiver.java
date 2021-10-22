package com.akapps.dailynote.classes.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {

    private int noteId;
    private int notePinNumber;
    private int allNoteSize;
    private String noteTitle;
    private boolean fingerprint;
    private boolean isChecklist;

    @Override
    public void onReceive(Context context, Intent intent) {
        noteId = intent.getIntExtra("id", -1);
        allNoteSize = intent.getIntExtra("size", -1);
        notePinNumber = intent.getIntExtra("pin", -1);
        noteTitle = intent.getStringExtra("title");
        fingerprint = intent.getBooleanExtra("fingerprint", false);
        isChecklist = intent.getBooleanExtra("checklist", false);
        NotificationHelper notificationHelper = new NotificationHelper(context, noteId,
                notePinNumber, noteTitle, fingerprint, isChecklist, allNoteSize);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(noteId, nb.build());
    }
}

