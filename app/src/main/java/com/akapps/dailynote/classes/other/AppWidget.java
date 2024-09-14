package com.akapps.dailynote.classes.other;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.activity.WidgetListView;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.data.User;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.RealmHelper;
import com.akapps.dailynote.classes.helpers.UiHelper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetId);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int noteId, int appWidgetId) {
        if (noteId == -1) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            Note currentNote = RealmHelper.getCurrentNote(context, noteId);
            User currentUser = RealmHelper.getUser(context, "in widget");
            if (currentNote != null) {
                boolean isLightTheme = UiHelper.getLightThemePreference(context);
                boolean isAllChecklistDone = isAllChecklistChecked(currentNote);
                // Construct the RemoteViews object
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                views.setTextViewText(R.id.appwidget_text, currentNote.getTitle().isEmpty() ? "~ No Title ~" : currentNote.getTitle());
                views.setInt(R.id.widget_background, "setBackgroundColor", currentNote.getBackgroundColor());
                if (isAllChecklistDone)
                    views.setInt(R.id.appwidget_text, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG);
                else
                    views.setInt(R.id.appwidget_text, "setPaintFlags", 0);

                if (currentNote.getTitleColor() != currentNote.getBackgroundColor())
                    views.setTextColor(R.id.appwidget_text, currentNote.getTitleColor());
                else
                    views.setTextColor(R.id.appwidget_text, context.getResources().getColor(R.color.white));

                views.setInt(R.id.preview_checklist, "setBackgroundResource", isLightTheme ? R.drawable.round_corner_light : R.drawable.round_corner);

                ArrayList<String> list = AppData.getNoteChecklist(noteId, context);

                Intent intent = new Intent(context, WidgetListView.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putStringArrayListExtra("list", list);
                intent.setData((Uri.fromParts("content",
                        String.valueOf(appWidgetId) + new Random().nextInt(10000), null)));
                intent.putExtra("id", currentNote.getNoteId());
                if(currentUser == null || currentUser.getWidgetTextSize() < 10)
                    intent.putExtra("text_size", 10);
                else
                    intent.putExtra("text_size", currentUser.getWidgetTextSize());
                views.setRemoteAdapter(R.id.preview_checklist, intent);

                if (currentNote.getPinNumber() == 0) {
                    intent = new Intent(context, NoteEdit.class);
                    intent.putExtra("id", currentNote.getNoteId());
                    intent.putExtra("isChecklist", currentNote.isCheckList());
                } else {
                    intent = new Intent(context, NoteLockScreen.class);
                    intent.putExtra("id", currentNote.getNoteId());
                    intent.putExtra("title", currentNote.getTitle().replace("\n", " "));
                    intent.putExtra("pin", currentNote.getPinNumber());
                    intent.putExtra("securityWord", currentNote.getSecurityWord());
                    intent.putExtra("fingerprint", currentNote.isFingerprint());
                }
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // if title or checklist is clicked, then open note
                views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
                views.setPendingIntentTemplate(R.id.preview_checklist, pendingIntent);

                // Instruct the widget manager to update the widget
                try {
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
                catch (Exception e){
                    updateAppWidget(context, appWidgetManager, noteId, appWidgetId);
                }
            }
        });
    }

    private static boolean isAllChecklistChecked(Note currentNote) {
        List<CheckListItem> results = new ArrayList<>();
        if (currentNote.isCheckList()) {
            for (CheckListItem item : currentNote.getChecklist()) {
                if (item.isChecked()) results.add(item);
            }
        }
        return results.size() == currentNote.getChecklist().size() && results.size() != 0;
    }
}