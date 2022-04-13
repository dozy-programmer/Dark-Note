package com.akapps.dailynote.classes.other;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import com.akapps.dailynote.R;
import com.akapps.dailynote.activity.NoteEdit;
import com.akapps.dailynote.activity.NoteLockScreen;
import com.akapps.dailynote.activity.WidgetConfigureActivity;
import com.akapps.dailynote.activity.WidgetListView;
import com.akapps.dailynote.classes.data.CheckListItem;
import com.akapps.dailynote.classes.data.Note;
import com.akapps.dailynote.classes.helpers.AppData;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.helpers.RealmDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import io.realm.Realm;
import io.realm.RealmResults;

public class AppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        // initialize database and get data

        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Here", "ON RECIEVE ");
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        CharSequence widgetText = WidgetConfigureActivity.loadTitlePref(context, appWidgetId);

        if(widgetText.equals("null"))
            return;

        ArrayList<Note> allNotes = AppData.getAllNotes(context);

        Note currentNote = getCurrentNote(allNotes, (String) widgetText);
        Log.d("Here", "Attempting to set Widget id for note " + currentNote.getTitle() + " is " + appWidgetId);
        AppData.updateNoteWidget(context, currentNote.getNoteId(), appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setInt(R.id.background, "setBackgroundColor", currentNote.getBackgroundColor());
        views.setTextColor(R.id.appwidget_text, currentNote.getTextColor());

        ArrayList<String> list = new ArrayList<>();
        if(currentNote.isCheckList()) {
            for(CheckListItem currentChecklist: currentNote.getChecklist())
                list.add(currentChecklist.getText());
        }
        else {
            String preview = Html.fromHtml(currentNote.getNote(), Html.FROM_HTML_MODE_COMPACT).toString();
            preview = preview.replaceAll("(\\s{2,})", " ");
            list.add(preview);
        }

        Intent intent = new Intent(context, WidgetListView.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putStringArrayListExtra("list", list);
        intent.setData((Uri.fromParts("content", String.valueOf(appWidgetId), null)));
        intent.putExtra("id", currentNote.getNoteId());
        views.setRemoteAdapter(R.id.preview_checklist, intent);

        if(currentNote.getPinNumber() == 0){
            intent = new Intent(context, NoteEdit.class);
            intent.putExtra("id", currentNote.getNoteId());
            intent.putExtra("isChecklist", currentNote.isCheckList());
        }
        else {
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
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static Note getCurrentNote(ArrayList<Note> allNotes, String noteTitle){
        List<Note> results = allNotes.stream()
                .filter(item -> item.getTitle().equals(noteTitle))
                .collect(Collectors.toList());

        return results.get(0);
    }
}