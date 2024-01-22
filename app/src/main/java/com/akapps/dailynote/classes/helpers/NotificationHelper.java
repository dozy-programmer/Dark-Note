package com.akapps.dailynote.classes.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationHelper extends ContextWrapper {
    // objects
    private NotificationManager mManager;

    // variables
    private final String channelName = "Note Reminders";
    private final String channelID = "channelID";
    private final String title = "Dark Note Reminder";
    private String content;
    private final String confirmButton = "OPEN";
    private final String dismissButton = "DISMISS";
    private final int noteId;
    private final int notePinNumber;
    private final String noteTitle;
    private final boolean fingerprint;

    public NotificationHelper(Context context, int noteId, int notePinNumber,
                              String noteTitle, boolean fingerprint) {
        super(context);
        this.noteId = noteId;
        this.notePinNumber = notePinNumber;
        this.noteTitle = noteTitle;
        this.fingerprint = fingerprint;
        createChannel();
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName,
                NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        return mManager == null ?
                mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE) : mManager;
    }

    public NotificationCompat.Builder getChannelNotification() {
        Intent activityIntent;
        content = "Reminder for " + (noteTitle.isEmpty() ? "Note" : noteTitle);

        // if note is locked, open note lock screen activity
        if (notePinNumber > 0) {
            activityIntent = new Intent(this, NoteLockScreen.class);
            activityIntent.putExtra("title", noteTitle);
            activityIntent.putExtra("pin", notePinNumber);
            activityIntent.putExtra("fingerprint", fingerprint);
        }
        // if note is not locked, open note
        else
            activityIntent = new Intent(this, NoteEdit.class);
        // data sent to both activities
        activityIntent.putExtra("id", noteId);
        activityIntent.putExtra("dismissNotification", true);
        activityIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        // pending intent to open Note when notification is clicked
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                noteId, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // pending intent to dismiss reminder notification when dismiss button is pressed
        Intent dIntent = new Intent(this, AlertDismissReceiver.class);
        dIntent.putExtra("notificationId", noteId);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(this, noteId, dIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.note_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.cancel, dismissButton, dismissIntent)
                .addAction(R.drawable.note_icon, confirmButton, contentIntent)
                .setContentIntent(contentIntent)
                .setOngoing(true);

        return builder;
    }
}