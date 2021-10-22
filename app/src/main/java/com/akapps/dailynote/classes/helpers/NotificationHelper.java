package com.akapps.dailynote.classes.helpers;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
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
    @TargetApi(Build.VERSION_CODES.O)
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
            activityIntent = new Intent(this, SettingsScreen.class);
            activityIntent.putExtra("backup", true);
            activityIntent.putExtra("size", allNotesSize);
            title = "Backup Reminder";
            content = "Backup your data.";
            buttonTitle = "Backup";
        }
        else {
            title = "Note Reminder";
            content = "Look at your note.";
            buttonTitle = "Open Note";
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
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.note_icon)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.note_icon, buttonTitle, contentIntent);
    }
}