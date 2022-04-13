package com.akapps.dailynote.classes.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.activity.SettingsScreen;

public class NotificationHelper extends ContextWrapper {
    public String channelID = "channelID";
    public String channelName = "Channel Name";
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
            content = "Reminder for " + noteTitle;
            buttonTitle = "OPEN";
            if (notePinNumber > 0) {
                activityIntent = new Intent(this, NoteLockScreen.class);
                activityIntent.putExtra("id", noteId);
                activityIntent.putExtra("title", noteTitle);
                activityIntent.putExtra("pin", notePinNumber);
                activityIntent.putExtra("fingerprint", fingerprint);
            } else {
                activityIntent = new Intent(this, NoteEdit.class);
                activityIntent.putExtra("id", noteId);
                activityIntent.putExtra("isChecklist", isCheckList);
            }
        }

        PendingIntent contentIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            contentIntent = PendingIntent.getActivity(this,
                    noteId, activityIntent, PendingIntent.FLAG_IMMUTABLE);
        }
        else
            contentIntent = PendingIntent.getActivity(this,
                    noteId, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.note_icon)
                .setColor(getApplicationContext().getColor(R.color.orange))
                .setContentIntent(contentIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true)
                .addAction(R.drawable.note_icon, buttonTitle, contentIntent);
        return builder;
    }
}