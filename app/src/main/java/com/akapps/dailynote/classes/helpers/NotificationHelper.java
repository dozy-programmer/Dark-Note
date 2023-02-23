package com.akapps.dailynote.classes.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.activity.SettingsScreen;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationHelper extends ContextWrapper {
    public String channelID = "channelID";
    public String channelName = "Note Reminders";
    private NotificationManager mManager;

    private int noteId;
    private int notePinNumber;
    private String noteTitle;
    private boolean fingerprint;
    private boolean isCheckList;
    private int allNotesSize;

    private String title;
    private String content;
    private String buttonTitle;

    public NotificationHelper(Context base, int noteId, int notePinNumber,
                              String noteTitle, boolean fingerprint, boolean isChecklist, int allNotesSize) {
        super(base);
        this.noteId = noteId;
        this.notePinNumber = notePinNumber;
        this.noteTitle = noteTitle;
        this.fingerprint = fingerprint;
        this.isCheckList = isChecklist;
        this.allNotesSize = allNotesSize;
        createChannel();
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(getColor(R.color.orange));
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build());
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null)
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification() {
        Intent activityIntent;
        if(noteId == 1234){
            title = "Dark Note Reminder";
            content = "Backup your data";
            buttonTitle = "BACKUP";
            activityIntent = new Intent(this, SettingsScreen.class);
            activityIntent.putExtra("backup", true);
            activityIntent.putExtra("size", allNotesSize);
        }
        else {
            title = "Dark Note Reminder";
            content = "Reminder for " + (noteTitle.isEmpty() ? "Note" : noteTitle);
            buttonTitle = "OPEN";
            if (notePinNumber > 0) {
                activityIntent = new Intent(this, NoteLockScreen.class);
                activityIntent.putExtra("id", noteId);
                activityIntent.putExtra("title", noteTitle);
                activityIntent.putExtra("pin", notePinNumber);
                activityIntent.putExtra("fingerprint", fingerprint);
                activityIntent.putExtra("dismissNotification", true);
            } else {
                activityIntent = new Intent(this, NoteEdit.class);
                activityIntent.putExtra("id", noteId);
                activityIntent.putExtra("isChecklist", isCheckList);
                activityIntent.putExtra("dismissNotification", true);
            }
            activityIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                    noteId, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //Create an Intent for the BroadcastReceiver
        Intent dIntent = new Intent(this, AlertDismissReceiver.class);
        dIntent.putExtra("notificationId", noteId);
        // Create the PendingIntent
        PendingIntent dismissIntent = PendingIntent.getBroadcast(this, noteId, dIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.note_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(contentIntent, true)
                .addAction(R.drawable.cancel, "DISMISS", dismissIntent)
                .addAction(R.drawable.note_icon, buttonTitle, contentIntent)
                .setContentIntent(contentIntent);

        return builder;
    }
}